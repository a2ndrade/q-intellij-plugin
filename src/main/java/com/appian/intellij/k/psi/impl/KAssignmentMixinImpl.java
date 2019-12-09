package com.appian.intellij.k.psi.impl;

import java.util.Optional;

import com.appian.intellij.k.KAstWrapperPsiElement;
import com.appian.intellij.k.KReference;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KAssignmentMixin;
import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KModeDirective;
import com.appian.intellij.k.psi.KTypes;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

public abstract class KAssignmentMixinImpl extends KAstWrapperPsiElement implements KAssignmentMixin {
  KAssignmentMixinImpl(ASTNode node) {
    super(node);
  }

  @Override
  public boolean isComposite() {
    // e.g. x[..]:1
    return (((KAssignment)this).getArgs()) != null;
  }

  @Override
  public boolean isNamespaced() {
    final KUserId userId = ((KAssignment)this).getUserId();
    return KUtil.isNamespacedId(userId.getText());
  }

  @Override
  public boolean isEffectiveGlobalAmend() {
    final KUserId userId = ((KAssignment)this).getUserId();
    return isSyntacticallyGlobalAmend() && KReference.findLocalDeclaration(userId) == null;
  }

  @Override
  public boolean isSyntacticallyGlobalAmend() {
    final KUserId userId = ((KAssignment)this).getUserId();
    // global amend e.g. a::1
    return Optional.of(userId)
        .map(PsiElement::getNextSibling)
        .filter(el -> el.getNode().getElementType() == KTypes.COLON)
        .map(PsiElement::getNextSibling)
        .filter(el -> el.getNode().getElementType() == KTypes.COLON)
        .isPresent();
  }

  @Override
  public boolean isTopLevelGlobal() {
    if (isSyntacticallyTopLevelGlobal()) {
      return true;
    }
    // if assignment is not within a lambda e.g.
    //   \d .ns
    //   if[1;someVar:123]
    return PsiTreeUtil.findFirstParent(this, true, (e) -> e instanceof KLambda) == null;
  }

  @Override
  public boolean isSyntacticallyTopLevelGlobal() {
    PsiElement current = this;
    // the loop is needed to correctly categorized chained top-level assignments e.g. x:y:z:1
    while (current instanceof KAssignment) {
      // immediate parent is always a KExpression
      current = current.getParent().getParent();
    }
    if (current instanceof KModeDirective || current instanceof KFile) {
      return true;
    }
    return false;
  }
}
