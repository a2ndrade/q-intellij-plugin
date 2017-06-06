package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;

public class KAstWrapperPsiElement extends ASTWrapperPsiElement {
  public KAstWrapperPsiElement(ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }

  @Override
  public PsiReference getReference() {
    return super.getReference();
  }


}
