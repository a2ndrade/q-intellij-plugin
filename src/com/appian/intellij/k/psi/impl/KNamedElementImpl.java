package com.appian.intellij.k.psi.impl;

import com.appian.intellij.k.KAstWrapperPsiElement;
import com.appian.intellij.k.psi.KNamedElement;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

public abstract class KNamedElementImpl extends KAstWrapperPsiElement implements KNamedElement {
  public KNamedElementImpl(ASTNode node) {
    super(node);
  }
}
