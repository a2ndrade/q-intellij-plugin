package com.appian.intellij.k.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface KNamedElement extends PsiNameIdentifierOwner {
  boolean isDeclaration();

  boolean isColumnDeclaration();

  boolean isInternal();

  ItemPresentation getPresentation();
}
