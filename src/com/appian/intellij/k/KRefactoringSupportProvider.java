package com.appian.intellij.k;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;

public final class KRefactoringSupportProvider extends RefactoringSupportProvider {
  @Override
  public boolean isMemberInplaceRenameAvailable(PsiElement element, PsiElement context) {
    return element instanceof KUserId;
  }
}
