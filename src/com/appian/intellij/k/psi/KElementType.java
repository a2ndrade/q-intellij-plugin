package com.appian.intellij.k.psi;

import com.appian.intellij.k.KLanguage;
import com.intellij.psi.tree.IElementType;

public final class KElementType extends IElementType {
  public KElementType(String debugName) {
    super(debugName, KLanguage.INSTANCE);
  }
}
