package com.fapiko.jetbrains.plugins.better_direnv.runconfigs;

import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.nodejs.execution.AbstractNodeTargetRunProfile;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings;
import com.jetbrains.nodejs.run.NodeJsRunConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NodeRunConfigurationTest {

    private final NodeRunConfiguration extension = new NodeRunConfiguration();

    @Test
    void isApplicableFor_nodeJsRunConfiguration_returnsTrue() {
        NodeJsRunConfiguration config = mock(NodeJsRunConfiguration.class);

        assertTrue(extension.isApplicableFor(config));
    }

    @Test
    void isApplicableFor_npmRunConfiguration_returnsTrue() {
        NpmRunConfiguration config = mock(NpmRunConfiguration.class);

        assertTrue(extension.isApplicableFor(config));
    }

    @Test
    void isApplicableFor_unrelatedRunProfile_returnsFalse() {
        AbstractNodeTargetRunProfile config = mock(AbstractNodeTargetRunProfile.class);

        assertFalse(extension.isApplicableFor(config));
    }

    @Test
    void createLaunchSession_nodeJsRunConfiguration_doesNotThrow() {
        NodeJsRunConfiguration config = mock(NodeJsRunConfiguration.class);
        when(config.getWorkingDirectory()).thenReturn("/tmp");
        when(config.getEnvs()).thenReturn(Collections.emptyMap());
        ExecutionEnvironment environment = mock(ExecutionEnvironment.class);

        assertDoesNotThrow(() -> extension.createLaunchSession(config, environment));

        verify(config).setEnvs(anyMap());
    }

    @Test
    void createLaunchSession_npmRunConfiguration_doesNotThrow() {
        NpmRunConfiguration config = mock(NpmRunConfiguration.class);
        NpmRunSettings runSettings = mock(NpmRunSettings.class);
        EnvironmentVariablesData envData = mock(EnvironmentVariablesData.class);

        when(config.getRunSettings()).thenReturn(runSettings);
        when(config.getEnvData()).thenReturn(envData);
        when(runSettings.getPackageJsonSystemDependentPath()).thenReturn("/tmp/project/package.json");
        when(runSettings.getArguments()).thenReturn("");
        when(runSettings.getCommand()).thenReturn(NpmCommand.RUN_SCRIPT);
        when(runSettings.getNodeOptions()).thenReturn("");
        when(runSettings.getScriptNames()).thenReturn(Collections.emptyList());
        when(runSettings.getInterpreterRef()).thenReturn(mock(NodeJsInterpreterRef.class));
        when(runSettings.getPackageManagerPackageRef()).thenReturn(mock(NodePackageRef.class));
        when(envData.getEnvs()).thenReturn(Collections.emptyMap());
        when(envData.isPassParentEnvs()).thenReturn(true);
        ExecutionEnvironment environment = mock(ExecutionEnvironment.class);

        assertDoesNotThrow(() -> extension.createLaunchSession(config, environment));

        verify(config).setRunSettings(any());
    }

    /**
     * Regression test for https://github.com/better-direnv/intellij-better-direnv/issues/64:
     * a run profile that is neither a {@link NodeJsRunConfiguration} nor an
     * {@link NpmRunConfiguration} (e.g. an anonymous NpmRunConfiguration subclass) must not
     * trigger a ClassCastException.
     */
    @Test
    void createLaunchSession_unrelatedRunProfile_doesNotThrow() {
        AbstractNodeTargetRunProfile config = mock(AbstractNodeTargetRunProfile.class);
        ExecutionEnvironment environment = mock(ExecutionEnvironment.class);

        assertDoesNotThrow(() -> extension.createLaunchSession(config, environment));
    }
}
