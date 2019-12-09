package com.appian.intellij.k;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;

public final class KUsageTypeProvider implements UsageTypeProvider {

  private static final UsageType ASSIGNMENTS = new UsageType("Assignments");
  private static final UsageType REFERENCES = new UsageType("References");

  @Nullable
  @Override
  public UsageType getUsageType(PsiElement element) {
    if (element instanceof KUserId) {
      final KUserId userId = (KUserId)element;
      if (userId.isDeclaration()) {
        return ASSIGNMENTS;
      }
      return REFERENCES;
    }
    return null;
  }
}
