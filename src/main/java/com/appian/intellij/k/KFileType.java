package com.appian.intellij.k;

import javax.swing.*;

import com.intellij.openapi.fileTypes.LanguageFileType;

public final class KFileType extends LanguageFileType {

  public static final KFileType INSTANCE = new KFileType();

  private KFileType() {
    super(KLanguage.INSTANCE);
  }

  @Override
  public String getName() {
    return "q file";
  }

  @Override
  public String getDescription() {
    return "q language file";
  }

  @Override
  public String getDefaultExtension() {
    return "q";
  }

  @Override
  public Icon getIcon() {
    return KIcons.FILE;
  }

}
