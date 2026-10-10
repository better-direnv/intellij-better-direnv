# Groovy Script run configurations and `RunConfigurationExtension`

The missing invocation of updateJAvaParameters is reported to Jetbrains as `IDEA-388745`.
## Summary

`org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration` extends
`com.intellij.execution.JavaRunConfigurationBase` and is, by class hierarchy, a
Java run configuration. However, unlike sibling Java run configurations
(`ApplicationConfiguration`, `JUnit*`, `TestNG*`, etc.), Groovy script runs
**bypass `JavaRunConfigurationExtensionManager`** at execution time. As a
result, third-party plugins that register a `<runConfigurationExtension>` under
`com.intellij` never get their `updateJavaParameters` / `patchCommandLine` /
`attachExtensionsToProcess` hooks called for Groovy script runs.

For *Better Direnv*, this means we cannot inject env vars derived from
`.envrc` into a Groovy Script run configuration's command line, even though
every other Java run configuration is covered by the same single extension.

## Verified against

- IntelliJ IDEA Ultimate 2026.1 (build 261)
- Bundled Groovy plugin from `plugins/Groovy/lib/Groovy.jar`
- Bundled Java exec from `plugins/java/lib/modules/intellij.java.execution.impl.jar`

## Detailed analysis

### 1. Class hierarchy looks Java-shaped

`javap -p` on `GroovyScriptRunConfiguration.class`:

```
public final class org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration
    extends com.intellij.execution.JavaRunConfigurationBase
    implements com.intellij.execution.CommonJavaRunConfigurationParameters,
               com.intellij.execution.configurations.RefactoringListenerProvider
```

So a `RunConfigurationExtension.isApplicableFor(RunConfigurationBase<?>)`
returning `true` will match Groovy script configs. The extension framework is
*willing* to apply the extension; nothing actually invokes it.

### 2. `getState(...)` returns a plain `JavaCommandLineState` that does not call the extension manager

`GroovyScriptRunConfiguration.getState(...)` returns an anonymous inner class
(`GroovyScriptRunConfiguration$1`) extending
`com.intellij.execution.configurations.JavaCommandLineState`. Its
`createJavaParameters()` simply delegates to the configuration's own
`createJavaParameters(VirtualFile, GroovyScriptRunner)` and returns the result:

```
class org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration$1
    extends com.intellij.execution.configurations.JavaCommandLineState {

  protected JavaParameters createJavaParameters() throws ExecutionException {
      return this$0.createJavaParameters(val$scriptFile, val$scriptRunner);
  }
}
```

There is no call to
`JavaRunConfigurationExtensionManager.getInstance().updateJavaParameters(...)`
anywhere in `GroovyScriptRunConfiguration` or its inner classes. A bytecode
scan of every class under `org/jetbrains/plugins/groovy/runner/` in
`Groovy.jar` finds zero references to `JavaRunConfigurationExtensionManager`.

For comparison, `BaseJavaApplicationCommandLineState` (the analogous base for
`ApplicationConfiguration` and friends) explicitly calls

```
JavaRunConfigurationExtensionManager.getInstance()
    .updateJavaParameters(myConfiguration, params, getRunnerSettings(), executor);
```

inside its `createJavaParameters()` flow. That call is what makes
`<runConfigurationExtension>` work for normal Java runs.

### 3. `JavaCommandLineState` itself does not dispatch extensions

`JavaCommandLineState.getJavaParameters()` only memoises and returns the result
of `createJavaParameters()`:

```
public JavaParameters getJavaParameters() throws ExecutionException {
    if (myParams == null) {
        myParams = isReadActionRequired()
            ? ReadAction.compute(this::createJavaParameters)
            : createJavaParameters();
    }
    return myParams;
}
```

No extension-manager calls here. The dispatch responsibility lives in concrete
subclasses, and Groovy's anonymous subclass does not perform it.

### 4. The Groovy plugin defines no equivalent extension point

`META-INF/plugin.xml` inside `Groovy.jar` exposes many extension points
(`methodComparator`, `membersContributor`, `astTransformationSupport`, etc.)
but none for run-configuration extension/customization. So there is no
Groovy-side hook that third parties could register against either.

### 5. Net effect

A plugin like *Better Direnv*, which registers a single
`<runConfigurationExtension implementation="...">` under
`defaultExtensionNs="com.intellij"`, is correctly applied to every other Java
run configuration but is silently skipped for Groovy script runs. The settings
tab may still render (depending on whether `appendEditors` is invoked from the
Groovy run-config form — it usually is, because the form goes through the
shared run-config UI), but `updateJavaParameters` is never called, so the env
contributed by the extension is never seen by the executed JVM. This produces
a confusing UX where the user configures direnv on the run config and the
settings appear to be saved but have no effect.

## Why this matters more broadly

Any plugin that uses the standard, documented pattern of registering a
`RunConfigurationExtension` for Java run configurations is broken for Groovy
scripts. This includes (at minimum) plugins doing:

- environment-variable injection from external sources (direnv, dotenv, secret
  managers)
- automatic JVM argument patching (profilers, instrumentation)
- console decoration / log capturing
- custom telemetry attachment via `attachExtensionsToProcess`

The existing API contract suggests these should "just work" for any
`JavaRunConfigurationBase` subclass; in practice, Groovy script runs silently
opt out.

## Proposed fix

In `GroovyScriptRunConfiguration#getState(...)`, the anonymous
`JavaCommandLineState` should invoke the extension manager exactly the way
`BaseJavaApplicationCommandLineState` does. Roughly:

```java
@Override
protected JavaParameters createJavaParameters() throws ExecutionException {
    JavaParameters params = GroovyScriptRunConfiguration.this
        .createJavaParameters(scriptFile, scriptRunner);
    JavaRunConfigurationExtensionManager.getInstance()
        .updateJavaParameters(
            GroovyScriptRunConfiguration.this,
            params,
            getRunnerSettings(),
            getEnvironment().getExecutor());
    return params;
}
```

This is a minimal, local change that aligns Groovy with the rest of the Java
run-config family.

## Workaround currently used

None usable. The available alternatives are all unsatisfying:

- A `BeforeRunTaskProvider` can pre-compute env, but writing it back into the
  configuration's persistent `envs` map mutates user-visible state and
  conflicts with manual edits.
- An `ExecutionListener.processStarting` runs after `JavaParameters` is
  finalised, so it cannot influence the JVM's environment.
- A custom `ProgramRunner` would need to wrap Groovy specifically, which
  requires duplicating Groovy's runner registration and is fragile across
  releases.

For now we plan to document that Groovy Script run configurations are not
supported and link to the upstream issue once filed.

---

# Suggested ticket text for JetBrains YouTrack

> **Title:** `GroovyScriptRunConfiguration` does not invoke
> `JavaRunConfigurationExtensionManager`, breaking
> `<runConfigurationExtension>` for Groovy script runs
>
> **Affected versions:** verified on 2026.1 (build 261); same code path
> appears to be present in earlier 2025.x releases.
>
> **Component:** Groovy plugin (`org.intellij.groovy`),
> `org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration`.
>
> **Summary**
>
> `GroovyScriptRunConfiguration` extends `JavaRunConfigurationBase`, so plugins
> that register a `RunConfigurationExtension` under
> `defaultExtensionNs="com.intellij"` and return `true` from
> `isApplicableFor(RunConfigurationBase<?>)` reasonably expect their hooks
> (`updateJavaParameters`, `patchCommandLine`, `attachExtensionsToProcess`,
> ...) to be invoked for Groovy script runs the same way they are for
> `ApplicationConfiguration`, `JUnit*`, `TestNG*`, etc.
>
> They are not. The anonymous `JavaCommandLineState` returned from
> `GroovyScriptRunConfiguration#getState(...)` does the following inside its
> `createJavaParameters()`:
>
> ```java
> protected JavaParameters createJavaParameters() throws ExecutionException {
>     return GroovyScriptRunConfiguration.this
>         .createJavaParameters(scriptFile, scriptRunner);
> }
> ```
>
> It never calls
> `JavaRunConfigurationExtensionManager.getInstance().updateJavaParameters(...)`.
> A scan of every class under `org/jetbrains/plugins/groovy/runner/` in
> `Groovy.jar` finds no reference to `JavaRunConfigurationExtensionManager`.
> `JavaCommandLineState` itself memoises `createJavaParameters()` without
> dispatching extensions, so nothing further along the chain compensates.
>
> By contrast, `BaseJavaApplicationCommandLineState` invokes
> `JavaRunConfigurationExtensionManager.getInstance().updateJavaParameters(myConfiguration, params, getRunnerSettings(), executor)`
> from its `createJavaParameters()` path. That call is what makes
> `<runConfigurationExtension>` work for the rest of the Java run-config
> family.
>
> **Steps to reproduce**
>
> 1. Build a plugin that registers
>
>    ```xml
>    <extensions defaultExtensionNs="com.intellij">
>      <runConfigurationExtension
>          implementation="..."
>          id="..." />
>    </extensions>
>    ```
>
>    where the extension implements
>    `com.intellij.execution.RunConfigurationExtension`,
>    returns `true` from `isApplicableFor(RunConfigurationBase<?>)`, and logs a
>    breakpoint inside `updateJavaParameters(...)`.
> 2. Install the plugin in IntelliJ IDEA Ultimate 2026.1.
> 3. Create a Groovy Script run configuration and run it.
>
> **Expected:** the extension's `updateJavaParameters` is invoked, mirroring
> the behaviour for a Java Application or JUnit run configuration.
>
> **Actual:** the extension is never invoked for Groovy script runs. A
> settings tab contributed by the extension may still render in the run-config
> dialog, but no env / JVM-arg / console hook fires at execution time, leading
> to a confusing UX where settings appear saved but have no effect.
>
> **Impact**
>
> Any plugin relying on the documented `RunConfigurationExtension` mechanism
> is silently broken for Groovy script runs. Examples:
>
> - environment-variable injection (direnv, dotenv, secret managers — this
>   issue was found while developing
>   [Better Direnv](https://github.com/better-direnv/intellij-better-direnv))
> - profilers / instrumentation / agents adding JVM args
> - console decoration / log capture
> - telemetry hookup via `attachExtensionsToProcess`
>
> **Suggested fix**
>
> Mirror the pattern used by `BaseJavaApplicationCommandLineState`:
>
> ```java
> @Override
> protected JavaParameters createJavaParameters() throws ExecutionException {
>     JavaParameters params = GroovyScriptRunConfiguration.this
>         .createJavaParameters(scriptFile, scriptRunner);
>     JavaRunConfigurationExtensionManager.getInstance()
>         .updateJavaParameters(
>             GroovyScriptRunConfiguration.this,
>             params,
>             getRunnerSettings(),
>             getEnvironment().getExecutor());
>     return params;
> }
> ```
>
> Localised to `GroovyScriptRunConfiguration` and consistent with the rest of
> the Java run-config family. `attachExtensionsToProcess(...)` could
> additionally be wired into the same anonymous state's `startProcess()` to
> cover console decoration.