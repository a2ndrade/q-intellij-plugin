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
    return "k file";
  }

  @Override
  public String getDescription() {
    return "k language file";
  }

  @Override
  public String getDefaultExtension() {
    return "k";
  }

  @Override
  public Icon getIcon() {
    return KIcons.FILE;
  }

}
