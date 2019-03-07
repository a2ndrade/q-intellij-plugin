package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;

public class KSpellcheckingStrategy extends SpellcheckingStrategy {

  @NotNull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof KUserId) {
      return EMPTY_TOKENIZER;
    }
    return super.getTokenizer(element);
  }
}
