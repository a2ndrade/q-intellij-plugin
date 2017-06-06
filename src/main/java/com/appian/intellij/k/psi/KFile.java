package com.appian.intellij.k.psi;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KFileType;
import com.appian.intellij.k.KLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public final class KFile extends PsiFileBase {
  public KFile(FileViewProvider viewProvider) {
    super(viewProvider, KLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return KFileType.INSTANCE;
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return super.getIcon(flags);
  }

  @Override
  public String toString() {
    return "k file";
  }
}
