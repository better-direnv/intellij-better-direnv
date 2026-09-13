# CLAUDE.md

Guidance for future Claude Code sessions working in this repo.

## Project shape

Multi-module Gradle project using the IntelliJ Platform Gradle Plugin
(`org.jetbrains.intellij.platform`, currently 2.18.1). Each IDE-specific
extension lives under `modules/products/<name>` (nodejs, python, phpstorm,
goland, rubymine, idea, shellscript), with shared code in `modules/core`.

Gradle module names are **not** their directory paths. `settings.gradle.kts`
rewrites `modules/products/nodejs` → `:better_direnv-products-nodejs` (the
`modules/` prefix becomes `better_direnv/`, then `/` becomes `-`). Always use
these rewritten names when targeting a module, e.g.:
```
./gradlew :better_direnv-products-nodejs:test
```
`:modules:products:nodejs:test` will fail with "project 'modules' not found".

## Compatibility range (`gradle.properties`: `pluginSinceBuild` / `pluginUntilBuild`)

- Build-number branch mapping: `261` = IntelliJ 2026.1, `262` = 2026.2, etc.
- **`pluginSinceBuild` must NOT contain a wildcard** (e.g. `261`, not `261.*`)
  — the plugin verifier (as shipped with Gradle plugin 2.18.1+) rejects a
  wildcard on since-build; wildcards are only valid on until-build.
- **Open-ended `pluginUntilBuild`**: leaving the property blank in
  `gradle.properties` is not enough by itself. In the root `build.gradle.kts`,
  `ideaVersion.untilBuild` must only be *assigned* when the property is
  non-blank — assigning an empty string produces a literal
  `<until-build></until-build>` in `plugin.xml`, which the verifier rejects
  outright ("attribute with only a branch number ()"). To go open-ended, the
  element must be omitted entirely, i.e. the Gradle property must be left
  unset, not set to `""`. See the guard around `ideaVersion { }` in
  `build.gradle.kts`.
- **Why the compatibility range matters beyond compiling**: JetBrains
  Marketplace serves whichever of a plugin's releases has an until-build that
  includes the requesting IDE's build number — if the latest release's
  until-build excludes a newer IDE, Marketplace silently falls back to an
  older, possibly long-unfixed release instead of erroring. This caused issue
  #64 (NPM run config crash on 2026.2): `pluginUntilBuild=261.*` on every
  release since 2026-04 meant 2026.2 users were served a 2023-era release
  with a bug fixed two years earlier in `main`. Verify what Marketplace would
  actually serve for a given build via:
  `https://plugins.jetbrains.com/plugins/list?pluginId=19275&build=<build>`
  (19275 is this plugin's Marketplace ID). A code fix on `main` does nothing
  for users until a release's compatibility range actually covers their IDE.

## `pluginVerification.failureLevel`

Default `failureLevel` under the 2.18.1 Gradle plugin includes
`INTERNAL_API_USAGES` and `EXPERIMENTAL_API_USAGES`, which will fail
`verifyPlugin` for pre-existing internal-API use in
`modules/products/python/.../PycharmEnvironmentProvider.java` (implements the
`@ApiStatus.Internal` interface `PythonCommandLineTargetEnvironmentProvider`).
`build.gradle.kts` currently excludes those two levels (see comment there,
tracked in issue #69) so this doesn't block builds. Don't silently widen this
further — if `verifyPlugin` starts failing on a *different* category, that's
a real compatibility problem, not more noise to suppress.

## Testing

No test infrastructure existed before issue #64's fix; `modules/core` had an
inert `testImplementation("org.junit.jupiter:junit-jupiter")` with no
`useJUnitPlatform()` anywhere, so it never actually ran. Root
`build.gradle.kts`'s `allprojects` block now configures
`tasks.withType<Test> { useJUnitPlatform() }` for every module.

To add tests to a product module that doesn't have a `src/test` yet:
- Add `testImplementation("org.junit.jupiter:junit-jupiter:<version>")`,
  `testImplementation("org.mockito:mockito-core:<version>")`, and
  `testRuntimeOnly("org.junit.platform:junit-platform-launcher")` to that
  module's `build.gradle.kts` (the launcher dependency is required — without
  it Gradle fails with "Could not load JUnit Platform", it is not pulled in
  automatically here).
- IntelliJ Platform classes referenced via `intellijPlatform { create(...);
  bundledPlugins(...) }` under `dependencies` are usable from test code too —
  `testImplementation` extends `implementation` by Gradle default, no extra
  wiring needed.
- Many bundled-plugin run-configuration classes (e.g.
  `NodeJsRunConfiguration`) are `final`. Mockito 5.x+ (`mockito-core`, no
  extra `mockito-inline` artifact needed) mocks final classes by default via
  its inline mock maker — just `mock(SomeFinalClass.class)` directly.
- Builder-style setters on platform model classes (e.g. `NpmRunSettings.Builder`)
  are frequently `@NotNull`-annotated on every parameter, so a test that
  round-trips one of these builders needs every relevant getter stubbed on the
  mock feeding it, not just the ones that seem load-bearing — an unstubbed
  `@NotNull` getter returns `null` and throws `IllegalArgumentException` deep
  in platform code, not in the code under test.
- See `modules/products/nodejs/src/test/java/.../NodeRunConfigurationTest.java`
  for a worked example (mocking `NodeJsRunConfiguration`/`NpmRunConfiguration`/
  `AbstractNodeTargetRunProfile`, and exercising the exact bug shape from #64:
  an `AbstractNodeTargetRunProfile` that is neither known subtype must not
  cause a `ClassCastException`).

## Debugging crashes reported against a released version

Before assuming a stack trace maps to current `main`, check whether it could
instead be an old, already-fixed release that's still being served — compare
`git log` / `git show <old-commit>:<path>` for the file at the version the
reporter's plugin build implies, and cross-check against what Marketplace
actually serves for their IDE build (see compatibility-range section above).
Pattern-matching `instanceof` (`if (x instanceof T t)`) cannot itself throw
`ClassCastException` — if a stack trace shows a CCE at a line that in current
`main` is an instanceof-guarded cast, look at what the code looked like in
the release actually reaching that user, not at `main`.
