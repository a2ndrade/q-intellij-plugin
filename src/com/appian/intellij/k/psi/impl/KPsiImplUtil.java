package com.appian.intellij.k.psi.impl;

import javax.swing.Icon;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.psi.KElementFactory;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public final class KPsiImplUtil {

  public static String getIdentifier(KUserId element) {
    final ASTNode keyNode = element.getNode().getFirstChildNode();
    if (keyNode != null) {
      return keyNode.getText();
    }
    return null;
  }

  public static String getName(KUserId element) {
    return getIdentifier(element);
  }

  public static PsiElement setName(KUserId element, String newName) {
    final ASTNode keyNode = element.getNode().getFirstChildNode();
    if (keyNode != null) {
      KUserId property = KElementFactory.createProperty(element.getProject(), newName);
      ASTNode newKeyNode = property.getFirstChild().getNode();
      element.getNode().replaceChild(keyNode, newKeyNode);
    }
    return element;
  }

  public static PsiElement getNameIdentifier(KUserId element) {
    final ASTNode keyNode = element.getNode().getFirstChildNode();
    if (keyNode != null) {
      return keyNode.getPsi();
    } else {
      return null;
    }
  }

  public static ItemPresentation getPresentation(final KUserId element) {
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return getIdentifier(element);
      }

      @Nullable
      @Override
      public String getLocationString() {
        final PsiFile containingFile = element.getContainingFile();
        return containingFile == null ? "" : containingFile.getName();
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return KIcons.FILE;
      }
    };
  }

}
