package com.fapiko.jetbrains.plugins.better_direnv.language.filetype;

import com.fapiko.jetbrains.plugins.better_direnv.language.DirenvLanguage;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.sh.ShIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DirenvFileType extends LanguageFileType {
    public static final DirenvFileType INSTANCE = new DirenvFileType();

    private DirenvFileType() {
        super(DirenvLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Direnv File";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return ".envrc";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "envrc";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Nodes.Console;
    }
}
