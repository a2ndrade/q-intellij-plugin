package com.appian.intellij.k;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KElementFactory;
import com.appian.intellij.k.psi.KNamespaceDeclaration;
import com.appian.intellij.k.psi.KSymbol;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public final class KSymbolicReference extends KReferenceBase {

  KSymbolicReference(KSymbol element, TextRange textRange) {
    super(element, textRange, false);
  }

  @NotNull
  @Override
  Collection<KUserId> resolve0(boolean stopAfterFirstMatch) {
    final String referenceName = myElement.getText().substring(1);
    return resolve00(referenceName, stopAfterFirstMatch);
  }

  @NotNull
  private Collection<KUserId> resolve00(String referenceName, boolean stopAfterFirstMatch) {
    final VirtualFile sameFile = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (sameFile == null || sameFile.getCanonicalPath() == null) {
      return Collections.emptyList();
    }
    final PsiElement context = myElement.getContext();
    if (context instanceof KNamespaceDeclaration) {
      return Collections.emptyList();
    }
    final Project project = myElement.getProject();

    final String fqn;
    if (KUtil.isAbsoluteId(referenceName)) {
      fqn = referenceName;
    } else {
      final String currentNs = KUtil.getCurrentNamespace(myElement);
      fqn = KUtil.generateFqn(currentNs, referenceName);
    }
    final KUtil.ExactGlobalAssignmentMatcher matcher = new KUtil.ExactGlobalAssignmentMatcher(fqn);
    return findDeclarations(project, sameFile, stopAfterFirstMatch, matcher);
  }

  @Override
  public PsiElement handleElementRename(@NotNull final String newName) throws IncorrectOperationException {
    final KUserId declaration = (KUserId)resolve();
    final String newEffectiveName = getNewNameForUsage(declaration, myElement, newName);
    final ASTNode keyNode = myElement.getNode().getFirstChildNode();
    KSymbol property = KElementFactory.createKSymbol(myElement.getProject(), toSymbolicName(newEffectiveName));
    ASTNode newKeyNode = property.getFirstChild().getNode();
    myElement.getNode().replaceChild(keyNode, newKeyNode);
    KUserIdCache.getInstance().remove(myElement);
    return myElement;
  }

  private String toSymbolicName(String name) {
    return name.charAt(0) == '`' ? name : "`" + name;
  }

}
