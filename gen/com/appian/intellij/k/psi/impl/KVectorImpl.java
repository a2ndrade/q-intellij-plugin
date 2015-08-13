// This is a generated file. Not intended for manual editing.
package com.appian.intellij.k.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.appian.intellij.k.psi.KTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.appian.intellij.k.psi.*;

public class KVectorImpl extends ASTWrapperPsiElement implements KVector {

  public KVectorImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KVisitor) ((KVisitor)visitor).visitVector(this);
    else super.accept(visitor);
  }

}
