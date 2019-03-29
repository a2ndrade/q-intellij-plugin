package com.appian.intellij.k.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface KNamedElement extends PsiNameIdentifierOwner {
  int UNKNOWN_ACCESS_LEVEL = -1;
  int PRIVATE_ACCESS_LEVEL = 0;
  int PUBLIC_ACCESS_LEVEL = 1;

  boolean isDeclaration();

  boolean isColumnDeclaration();

  boolean isInternal();

  ItemPresentation getPresentation();

  int getAccessLevel();

  @NotNull
  String getName();
}
