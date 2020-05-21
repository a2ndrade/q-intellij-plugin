package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;

public class KRenamePsiElementProcessor extends RenamePsiElementProcessor {

  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return element instanceof KUserId;
  }

  @NotNull
  @Override
  public Collection<PsiReference> findReferences(
      @NotNull PsiElement element, @NotNull SearchScope searchScope, boolean searchInCommentsAndStrings) {
    final Collection<PsiReference> additionalRefs = new ArrayList<>();
    if (element instanceof KUserId) {
      final String text = element.getText();
      final KReference ref = new KReference((KUserId)element, new TextRange(0, text.length()));
      for (KUserId id : ref.resolveAllOrdered()) {
        if (id == element) {
          continue;
        }
        final String idText = id.getText();
        final KReference idRef = new KReference(id, new TextRange(0, idText.length()));
        additionalRefs.add(idRef);
      }
    }
    List<PsiReference> all = new ArrayList<>();
    all.addAll(super.findReferences(element, searchScope, searchInCommentsAndStrings));
    all.addAll(additionalRefs);
    return all;
  }
}
