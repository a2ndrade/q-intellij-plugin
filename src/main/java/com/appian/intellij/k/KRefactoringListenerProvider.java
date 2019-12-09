package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KSymbol;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;

public final class KRefactoringListenerProvider implements RefactoringElementListenerProvider {
  @Nullable
  @Override
  public RefactoringElementListener getListener(PsiElement element) {
    if (element instanceof KUserId || element instanceof KSymbol) {
      return new RefactoringElementAdapter() {
        @Override
        protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
          KUserIdCache.getInstance().remove(element);
          KUserIdCache.getInstance().remove(newElement);
        }
        @Override
        public void undoElementMovedOrRenamed(
            @NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
          elementRenamedOrMoved(newElement);
        }
      };
    }
    return null;
  }
}
