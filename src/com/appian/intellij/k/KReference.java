package com.appian.intellij.k;

import com.appian.intellij.k.psi.KUserId;
import com.appian.intellij.k.psi.impl.KPsiImplUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;

public final class KReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

  private final String key;

  public KReference(PsiElement element, TextRange textRange) {
    super(element, textRange);
    this.key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
  }

  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final VirtualFile file = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (file == null) {
      return new ResolveResult[0];
    }
    final Project project = myElement.getProject();
    return KUtil.findIdentifiers(project, file)
      .filter(id -> id.getText().equals(key))
      .map(PsiElementResolveResult::new)
      .toArray(size -> new ResolveResult[size]);
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @Override
  public Object[] getVariants() {
    final VirtualFile file = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (file == null) {
      return new Object[0];
    }
    final Project project = myElement.getProject();
    return KUtil.findIdentifiers(project, file)
      .filter(identifier -> identifier.getName().length() > 0)
      .map(identifier -> LookupElementBuilder.create(identifier)
        .withIcon(KIcons.FILE)
        .withTypeText(identifier.getContainingFile().getName()))
      .toArray(size -> new LookupElement[size]);
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    if (myElement instanceof KUserId) {
      // inline rename
      KPsiImplUtil.setName((KUserId)myElement, newElementName);
      return myElement;
    }
    // cross-language rename
    return super.handleElementRename(newElementName);
  }
}
