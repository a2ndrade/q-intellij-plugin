package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KTypes;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

public final class KFindUsagesProvider implements FindUsagesProvider {

  @Nullable
  @Override
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new KLexerAdapter(), TokenSet.create(KTypes.USER_ID), TokenSet.EMPTY,
        TokenSet.EMPTY);
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    if (psiElement instanceof KUserId) {
      final PsiElement context = psiElement.getContext();
      return context instanceof KAssignment || context instanceof KLambdaParams;
    }
    return false;
  }

  @Nullable
  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @NotNull
  @Override
  public String getType(@NotNull PsiElement element) {
    if (element instanceof KUserId) {
      return KUtil.getFunctionDefinition((KUserId)element).isPresent() ? "function" : "variable";
    }
    return "";
  }

  @NotNull
  @Override
  public String getDescriptiveName(@NotNull PsiElement element) {
    if (element instanceof KUserId) {
      return KUtil.getDescriptiveName((KUserId)element);
    }
    return "";
  }

  @NotNull
  @Override
  public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    if (!(element instanceof KUserId)) {
      return "";
    }
    final KUserId userId = (KUserId)element;
    return useFullName ? userId.getDetails().getFqn() : userId.getName();
  }
}
