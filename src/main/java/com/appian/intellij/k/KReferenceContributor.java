package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KSymbol;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;

public final class KReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    // references from same language
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(KUserId.class).withLanguage(KLanguage.INSTANCE),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
            if (!(element instanceof KUserId)) {
              return new PsiReference[0];
            }
            final KUserId id = (KUserId)element;
            return new PsiReference[] {new KReference(id, new TextRange(0, id.getText().length()))};
          }
        });
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(KSymbol.class).withLanguage(KLanguage.INSTANCE),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
            if (element instanceof KSymbol) {
              final String key = element.getText();
              if (key.length() <= 1 || key.charAt(1) == ':' || key.charAt(1) == '/') {
                return new PsiReference[0];
              }
              return new PsiReference[] {new KSymbolicReference((KSymbol)element, new TextRange(1, key.length()))};
            }
            return new PsiReference[0];
          }
        });
  }
}
