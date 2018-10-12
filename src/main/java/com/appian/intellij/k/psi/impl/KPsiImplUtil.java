package com.appian.intellij.k.psi.impl;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.KUserIdCache;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KElementFactory;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KNamespaceDeclaration;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public final class KPsiImplUtil {

  @NotNull
  public static String getName(KUserId element) {
    return element.getNode().getFirstChildNode().getText();
  }

  public static PsiElement setName(KUserId element, String newName) {
    final ASTNode keyNode = element.getNode().getFirstChildNode();
    KUserId property = KElementFactory.createProperty(element.getProject(), newName);
    ASTNode newKeyNode = property.getFirstChild().getNode();
    element.getNode().replaceChild(keyNode, newKeyNode);
    KUtil.putFqn(element, null); // clear so it's recalculated next time
    KUserIdCache.getInstance().remove(element); // clear file cache to reflect changes immediately
    return element;
  }

  public static PsiElement getNameIdentifier(KUserId element) {
    return element.getNode().getFirstChildNode().getPsi();
  }

  public static ItemPresentation getPresentation(final KUserId element) {
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return KUtil.getFqnOrName(element);
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

  public static boolean isCompound(final KAssignment element) {
    return element.getArgs() != null;
  }

  public static boolean isDeclaration(final KUserId element) {
    final PsiElement parent = element.getParent();
    if (parent instanceof KLambdaParams || parent instanceof KNamespaceDeclaration) {
      return true;
    }
    if (parent instanceof KAssignment) {
      return !((KAssignment)parent).isCompound();
    }
    return false;
  }

}
