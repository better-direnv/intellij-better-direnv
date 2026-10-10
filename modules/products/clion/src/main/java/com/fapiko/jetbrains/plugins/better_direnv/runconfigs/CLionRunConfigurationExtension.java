package com.fapiko.jetbrains.plugins.better_direnv.runconfigs;

import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.RunnerSettings;
import com.jetbrains.cidr.execution.CidrRunConfigurationExtensionBase;
import com.jetbrains.cidr.execution.ConfigurationExtensionContext;
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment;
import com.jetbrains.cidr.lang.workspace.OCRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CLionRunConfigurationExtension extends CidrRunConfigurationExtensionBase {
    @Override
    public boolean isEnabledFor(@NotNull OCRunConfiguration ocRunConfiguration, @NotNull CidrToolEnvironment cidrToolEnvironment, @Nullable RunnerSettings runnerSettings) {
        return true;
    }

    @Override
    public boolean isApplicableFor(@NotNull OCRunConfiguration<?, ?> configuration) {
        return true;
    }

    @Override
    public void patchCommandLineState(@NotNull OCRunConfiguration configuration, @Nullable RunnerSettings runnerSettings, @NotNull CidrToolEnvironment environment, @Nullable File projectBaseDir, @NotNull CommandLineState state, @NotNull String runnerId, @NotNull ConfigurationExtensionContext context) {
        System.out.println("patchCommandLine");
    }
}
