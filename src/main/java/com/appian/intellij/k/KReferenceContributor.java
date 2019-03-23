package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

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
            if (element instanceof KUserId) {
              final String key = element.getText();
              return new PsiReference[] {new KReference((KUserId)element, new TextRange(0, key.length()))};
            }
            return new PsiReference[0];
          }
        });
  }
}
