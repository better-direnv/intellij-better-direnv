package com.fapiko.jetbrains.plugins.better_direnv.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class DirenvCmd {
    private static final String GROUP_DISPLAY_ID = "Better Direnv";
    private static final Logger LOG = Logger.getInstance(DirenvCmd.class);
    private static final String MINIMAL_DIRENV_VERSION = "2.8.0"; // introduced JSON export in this version

    private String workDir;

    public DirenvCmd(String workingDirectory) {
        this.workDir = workingDirectory;
    }

    private boolean allow() {
        try {
            DirenvOutput output = run("allow");
            if (output.isError()) {
                LOG.error(output.getOutput());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOG.error(e);
            return false;
        }
    }

    public Map<String, String> importDirenv(boolean trustDirenv) {
        try {
            if (!versionSupported()) {
                Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Direnv version not supported",
                        "Better Direnv requires direnv version " + MINIMAL_DIRENV_VERSION + " or higher",
                        NotificationType.WARNING));
                return Map.of();
            }
            DirenvOutput output = run("export", "json");
            if (output.isError()) {
                if (output.getOutput().contains("is blocked") && trustDirenv) {
                    if (allow()) {
                        return importDirenv(trustDirenv);
                    } else {
                        Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Direnv allow failed",
                                "Failed to run `direnv allow` command", NotificationType.WARNING));
                        return Map.of();
                    }
                } else {
                    Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Direnv not allowed",
                            "Either run `direnv allow` on a terminal or check the `Trust .envrc` box in the" +
                                    "run configuration settings to use direnv integration", NotificationType.WARNING));
                    return Map.of();
                }
            }

            // Output will be empty if there is no direnv support
            if (output.getOutput().isEmpty()) {
                return Map.of();
            }

            Type type = new TypeToken<Map<String, String>>() {}.getType();

            Map<String, String> result = new Gson().fromJson(output.getOutput(), type);
            return Optional.ofNullable(result)
                .flatMap(m -> {
                    m.remove("DIRENV_WATCHES");
                    m.remove("DIRENV_DIR");
                    m.remove("DIRENV_DIFF");
                    m.remove("DIRENV_FILE");
                    return Optional.of(m);
                })
                .orElse(Map.of());
        } catch (Exception e) {
            LOG.error(e);
            return Map.of();
        }
    }

    private boolean versionSupported() throws IOException, ExecutionException, InterruptedException {
        DirenvOutput output = run("version", MINIMAL_DIRENV_VERSION);
        return !output.isError();
    }

    private DirenvOutput run(String... args) throws ExecutionException, InterruptedException, IOException {
        String[] newArgArray = new String[1];
        newArgArray[0] = "direnv";
        newArgArray = ArrayUtils.addAll(newArgArray, args);

        GeneralCommandLine cli = new GeneralCommandLine(newArgArray).
                withWorkDirectory(workDir);
        Process process = cli.createProcess();

        if (process.waitFor() != 0) {
            String stdErr = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
            DirenvOutput output = new DirenvOutput(stdErr, true);
            return output;
        }

        String stdOut = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);

        return new DirenvOutput(stdOut, false);
    }
}
