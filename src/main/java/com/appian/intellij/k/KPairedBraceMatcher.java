package com.appian.intellij.k;

import com.appian.intellij.k.psi.KTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

final class KPairedBraceMatcher implements PairedBraceMatcher {

  private static BracePair[] PAIRS = new BracePair[] {new BracePair(KTypes.OPEN_BRACKET, KTypes.CLOSE_BRACKET, true),
      new BracePair(KTypes.OPEN_BRACE, KTypes.CLOSE_BRACE, true),
      new BracePair(KTypes.OPEN_PAREN, KTypes.CLOSE_PAREN, true)
  };

  public BracePair[] getPairs() {
    return PAIRS;
  }

  public boolean isPairedBracesAllowedBeforeType(IElementType lbraceType, IElementType contextType) {
    return true;
  }

  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }

}
