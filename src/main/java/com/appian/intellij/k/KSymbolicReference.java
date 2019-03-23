package com.appian.intellij.k;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KElementFactory;
import com.appian.intellij.k.psi.KNamespaceDeclaration;
import com.appian.intellij.k.psi.KSymbolOrRef;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;

public final class KSymbolicReference extends PsiReferenceBase<PsiElement> implements PsiReference {

  public KSymbolicReference(KSymbolOrRef element, TextRange textRange) {
    super(element, textRange);
  }

  @Override
  public PsiElement resolve() {
    final PsiElement reference = resolve0();
    // avoid including a variable declaration as a reference to itself
    return reference == myElement ? null : reference;
  }

  private PsiElement resolve0() {
    final KSymbolOrRef reference = (KSymbolOrRef)myElement;
    final String referenceName = reference.getText().substring(1);
    return resolve00(referenceName);
  }

  private PsiElement resolve00(String referenceName) {
    final VirtualFile sameFile = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (sameFile == null || sameFile.getCanonicalPath() == null) {
      return null;
    }
    final String sameFilePath = sameFile.getCanonicalPath();
    final PsiElement context = myElement.getContext();
    if (context instanceof KNamespaceDeclaration) {
      return null;
    }
    final Project project = myElement.getProject();
    final KUserIdCache cache = KUserIdCache.getInstance();
    final String fqn;
    if (KUtil.isAbsoluteId(referenceName)) {
      fqn = referenceName;
    } else {
      final String currentNs = KUtil.getCurrentNamespace(myElement);
      fqn = KUtil.generateFqn(currentNs, referenceName);
    }
    // 1) check same-file globals
    KUserId foundInSameFile = cache.findFirstExactMatch(project, sameFile, fqn);
    if (foundInSameFile != null) {
      return foundInSameFile;
    }
    // 2) check other file's globals
    final Collection<VirtualFile> otherFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    for (VirtualFile otherFile : otherFiles) {
      if (sameFilePath.equals(otherFile.getCanonicalPath())) {
        continue; // already processed above
      }
      KUserId foundInOtherFile = cache.findFirstExactMatch(project, otherFile, fqn);
      if (foundInOtherFile != null) {
        return foundInOtherFile;
      }
    }
    return null;
  }

  @Override
  public Object[] getVariants() {
    return new Object[0];
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newName) throws IncorrectOperationException {
    final KUserId declaration = ((KUserId)resolve());
    final String newEffectiveName = toSymbolicName(KReference.getNewNameForUsage(declaration, myElement, newName));
    final ASTNode keyNode = myElement.getNode().getFirstChildNode();
    KSymbolOrRef property = KElementFactory.createKSymbolOrRef(myElement.getProject(), newEffectiveName);
    ASTNode newKeyNode = property.getFirstChild().getNode();
    myElement.getNode().replaceChild(keyNode, newKeyNode);
    return myElement;
  }

  private String toSymbolicName(String name) {
    return name.charAt(0) == '`' ? name : "`" + name;
  }

}
