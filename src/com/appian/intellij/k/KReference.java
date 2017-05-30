package com.appian.intellij.k;

import java.util.Collection;
import java.util.Optional;

import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KLocalAssignment;
import com.appian.intellij.k.psi.KNamespaceDefinition;
import com.appian.intellij.k.psi.KUserId;
import com.appian.intellij.k.psi.impl.KPsiImplUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

public final class KReference extends PsiReferenceBase<PsiElement> implements PsiReference {

  public KReference(KUserId element, TextRange textRange) {
    super(element, textRange);
  }

  @Override
  public PsiElement resolve() {
    final VirtualFile file = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (file == null) {
      return null;
    }
    final PsiElement context = myElement.getContext();
    if (context instanceof KNamespaceDefinition) {
      return null;
    }
    final Project project = myElement.getProject();
    final KLambda lambda = PsiTreeUtil.getContextOfType(myElement, KLambda.class);
    final String targetName = ((KUserId)myElement).getName();
    if (targetName == null) {
      return null;
    }
    final KUserId foundInSameFile = Optional.ofNullable(lambda)
        .map(KLambda::getLambdaParams) // 1) check the enclosing lambda params
        .map(l -> l.getUserIdList()
            .stream()
            .filter(id -> targetName.equals(id.getName()))
            .findFirst()
            .orElse(PsiTreeUtil.findChildrenOfType(lambda, KLocalAssignment.class) // 2) check locals
                .stream()
                .map(KLocalAssignment::getUserId)
                .filter(id -> targetName.equals(id.getName()))
                .findFirst()
                .orElse(null)))
        .orElse(KUtil.findMatchingIdentifier(project, file, targetName)); // 3) check same-file globals
    if (foundInSameFile != null) {
      return foundInSameFile;
    }
    // 4) check other file's globals
    final Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      final KUserId foundInOtherFile = KUtil.findMatchingIdentifier(project, virtualFile, targetName);
      if (foundInOtherFile != null) {
        return foundInOtherFile;
      }
    }
    return null;
  }

  @Override
  public Object[] getVariants() {
    final VirtualFile file = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (file == null) {
      return new Object[0];
    }
    final Project project = myElement.getProject();
    return KUtil.findFileIdentifiers(project, file).stream()
      .filter(identifier -> identifier.getName() != null && identifier.getName().length() > 0)
      .map(identifier -> LookupElementBuilder.create(identifier)
        .withIcon(KIcons.FILE)
        .withTypeText(identifier.getContainingFile().getName()))
      .toArray(LookupElement[]::new);
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    if (myElement instanceof KUserId) {
      // inline rename
      KPsiImplUtil.setName((KUserId)myElement, newElementName);
      KUserIdCache.getInstance().remove(myElement.getContainingFile().getVirtualFile());
      return myElement;
    }
    // cross-language rename
    return super.handleElementRename(newElementName);
  }
}
