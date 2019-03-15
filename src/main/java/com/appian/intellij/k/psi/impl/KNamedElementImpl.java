package com.appian.intellij.k.psi.impl;

import com.appian.intellij.k.KAstWrapperPsiElement;
import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.KUserIdCache;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class KNamedElementImpl extends KAstWrapperPsiElement implements KNamedElement {
  KNamedElementImpl(ASTNode node) {
    super(node);
  }

  @NotNull
  public String getName() {
    return getNode().getFirstChildNode().getText();
  }

  public PsiElement setName(@NotNull String newName) {
    final ASTNode keyNode = getNode().getFirstChildNode();
    KUserId property = KElementFactory.createProperty(getProject(), newName);
    ASTNode newKeyNode = property.getFirstChild().getNode();
    getNode().replaceChild(keyNode, newKeyNode);
    KUtil.putFqn(this, null); // clear so it's recalculated next time
    KUserIdCache.getInstance().remove(this); // clear file cache to reflect changes immediately
    return this;
  }

  public PsiElement getNameIdentifier() {
    return getNode().getFirstChildNode().getPsi();
  }

  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @NotNull
      @Override
      public String getPresentableText() {
        return KUtil.getFqnOrName(KNamedElementImpl.this);
      }

      @NotNull
      @Override
      public String getLocationString() {
        final PsiFile containingFile = getContainingFile();
        return containingFile == null ? "" : containingFile.getName();
      }

      @NotNull
      @Override
      public Icon getIcon(boolean unused) {
        if (isDeclaration()) {
          if (KUtil.getFunctionDefinition(KNamedElementImpl.this).isPresent()) {
            return isInternal() ? KIcons.PRIVATE_FUNCTION : KIcons.PUBLIC_FUNCTION;
          } else {
            return isInternal() ? KIcons.PRIVATE_VARIABLE : KIcons.PUBLIC_VARIABLE;
          }
        }
        return KIcons.FILE;
      }
    };
  }

  public boolean isDeclaration() {
    final PsiElement parent = getParent();
    if (parent instanceof KLambdaParams || parent instanceof KNamespaceDeclaration) {
      return true;
    }
    if (parent instanceof KAssignment) {
      return ((KAssignment) parent).getArgs() == null;
    }
    return false;
  }

  public boolean isInternal() {
    final String name = getName();
    return name.startsWith("i.") || name.contains(".i.");
  }
}
