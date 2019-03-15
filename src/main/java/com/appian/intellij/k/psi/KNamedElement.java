package com.appian.intellij.k.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

public interface KNamedElement extends PsiNameIdentifierOwner {
    boolean isDeclaration();
    boolean isInternal();
}
