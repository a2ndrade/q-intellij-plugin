package com.appian.intellij.k;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;

public final class KPairedBraceMatcherAdapter extends PairedBraceMatcherAdapter {

  public KPairedBraceMatcherAdapter() {
    super(new KPairedBraceMatcher(), KLanguage.INSTANCE);
  }
}
