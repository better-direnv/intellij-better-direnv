package com.fapiko.jetbrains.plugins.better_direnv.runconfigs;

import com.fapiko.jetbrains.plugins.better_direnv.settings.DirenvSettings;
import com.fapiko.jetbrains.plugins.better_direnv.settings.ui.RunConfigSettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import com.jetbrains.python.run.PythonExecution;
import com.jetbrains.python.run.PythonRunParams;
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest;
import com.jetbrains.python.run.target.PythonCommandLineTargetEnvironmentProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage") // for target environments (Docker, SSH, WSL) this is the only extension mechanism
public class PycharmEnvironmentProvider implements PythonCommandLineTargetEnvironmentProvider {

    @Override
    public void extendTargetEnvironment(@NotNull Project project, @NotNull HelpersAwareTargetEnvironmentRequest helpersAwareTargetEnvironmentRequest, @NotNull PythonExecution pythonExecution, @NotNull PythonRunParams pythonRunParams) {
        if (!(pythonRunParams instanceof AbstractPythonRunConfiguration<?> runConfig)) {
            return;
        }
        DirenvSettings direnvSettings = runConfig.getCopyableUserData(RunConfigSettingsEditor.USER_DATA_KEY);
        Map<String, String> direnvVariables = RunConfigSettingsEditor.collectEnv(direnvSettings, pythonRunParams.getWorkingDirectory());
        Map<String, String> runConfigurationVariables = pythonRunParams.getEnvs();

        Stream.concat(direnvVariables.entrySet().stream(), runConfigurationVariables.entrySet().stream())
              .forEach(addEnvironmentVariableToPythonExecution(pythonExecution));
    }

    @NotNull
    private static Consumer<Map.Entry<String, String>> addEnvironmentVariableToPythonExecution(@NotNull PythonExecution pythonExecution) {
        return entry -> pythonExecution.addEnvironmentVariable(entry.getKey(), entry.getValue());
    }
}
