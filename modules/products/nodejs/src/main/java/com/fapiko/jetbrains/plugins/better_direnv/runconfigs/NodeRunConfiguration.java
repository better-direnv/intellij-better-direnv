package com.fapiko.jetbrains.plugins.better_direnv.runconfigs;

import com.fapiko.jetbrains.plugins.better_direnv.settings.ui.RunConfigSettingsEditor;
import com.intellij.execution.Executor;
import com.intellij.execution.Location;
import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.nodejs.execution.AbstractNodeTargetRunProfile;
import com.intellij.javascript.nodejs.execution.runConfiguration.AbstractNodeRunConfigurationExtension;
import com.intellij.javascript.nodejs.execution.runConfiguration.NodeRunConfigurationLaunchSession;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.NlsSafe;
import com.jetbrains.nodejs.run.NodeJsRunConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class NodeRunConfiguration extends AbstractNodeRunConfigurationExtension {
    @Override
    protected void readExternal(@NotNull AbstractNodeTargetRunProfile runConfiguration, @NotNull Element element) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element);
    }

    @Override
    protected void writeExternal(@NotNull AbstractNodeTargetRunProfile runConfiguration, @NotNull Element element) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element);
    }

    @NotNull
    @Override
    public <P extends AbstractNodeTargetRunProfile> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new RunConfigSettingsEditor<>(configuration);
    }

    @Override
    public boolean isApplicableFor(@NotNull AbstractNodeTargetRunProfile configuration) {
        return (configuration instanceof NodeJsRunConfiguration) || (configuration instanceof NpmRunConfiguration);
    }

    @Override
    protected void patchCommandLine(@NotNull AbstractNodeTargetRunProfile configuration, @Nullable RunnerSettings runnerSettings, @NotNull GeneralCommandLine cmdLine, @NotNull String runnerId, @NotNull Executor executor) {
        configuration.getSelectedOptions();
    }

    @Override
    protected void extendCreatedConfiguration(@NotNull AbstractNodeTargetRunProfile configuration, @NotNull Location location) {
        super.extendCreatedConfiguration(configuration, location);
    }

    @Nullable
    @Override
    public String getEditorTitle() {
        return RunConfigSettingsEditor.getEditorTitle();
    }

    @Nullable
    @Override
    public NodeRunConfigurationLaunchSession createLaunchSession(@NotNull AbstractNodeTargetRunProfile configuration, @NotNull ExecutionEnvironment environment) {
        if (configuration instanceof NodeJsRunConfiguration config) {

            Map<String, String> newEnvs = RunConfigSettingsEditor
                .collectEnv(configuration, config.getWorkingDirectory(), config.getEnvs());

            config.setEnvs(newEnvs);
        } else if (configuration instanceof com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration config) {
            @NotNull @NlsSafe File packagePath = new File(config.getRunSettings().getPackageJsonSystemDependentPath());
            @NotNull String workingDirectory = packagePath.getParent();

            @NotNull Map<String, String> envData = config.getEnvData().getEnvs();
            Map<String, String> newEnvs = RunConfigSettingsEditor
                .collectEnv(configuration, workingDirectory, envData);

            NpmRunSettings runSettings = config.getRunSettings();
            @NotNull EnvironmentVariablesData newEnvData = EnvironmentVariablesData.create(newEnvs, config.getEnvData().isPassParentEnvs());
            NpmRunSettings newRunSettings = new NpmRunSettings(
                NpmRunSettings
                    .builder()
                    .setArguments(runSettings.getArguments())
                    .setCommand(runSettings.getCommand())
                    .setNodeOptions(runSettings.getNodeOptions())
                    .setEnvData(newEnvData)
                    .setInterpreterRef(runSettings.getInterpreterRef())
                    .setPackageJsonPath(runSettings.getPackageJsonSystemDependentPath())
                    .setPackageManagerPackageRef(runSettings.getPackageManagerPackageRef())
                    .setScriptNames(runSettings.getScriptNames())
            );
            config.setRunSettings(newRunSettings);
        }
        return null;
    }
}
