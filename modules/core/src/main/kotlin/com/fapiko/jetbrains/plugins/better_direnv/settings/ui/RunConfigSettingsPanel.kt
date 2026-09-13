package com.fapiko.jetbrains.plugins.better_direnv.settings.ui

import com.fapiko.jetbrains.plugins.better_direnv.settings.DirenvSettings
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.JComponent

class RunConfigSettingsPanel(configuration: RunConfigurationBase<*>) {
    private val enableDirenvCheckBox = JCheckBox("Enable Direnv")
    private val trustEnvrcCheckBox = JCheckBox("Trust .envrc")

    private val panel = panel {
        row {
            cell(enableDirenvCheckBox)
        }
        indent {
            row {
                cell(trustEnvrcCheckBox)
            }
            row {
                comment(
                    """When enabled it will automatically allow direnv to process changes to the .envrc file in
                       the working directory. Only enable this for projects you trust, direnv can execute
                       potentially malicious code.""".trimIndent()
                )
            }
        }
    }

    val component: JComponent get() = panel

    fun addChangeListener(listener: Runnable) {
        enableDirenvCheckBox.addItemListener { listener.run() }
        trustEnvrcCheckBox.addItemListener { listener.run() }
    }

    fun getState(): DirenvSettings {
        return DirenvSettings(enableDirenvCheckBox.isSelected, trustEnvrcCheckBox.isSelected)
    }

    fun setState(state: DirenvSettings) {
        enableDirenvCheckBox.isSelected = state.isDirenvEnabled()
        trustEnvrcCheckBox.isSelected = state.isDirenvTrusted()
    }
}