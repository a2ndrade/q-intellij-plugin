package com.appian.intellij.k;

import static com.appian.intellij.k.KCompletionContributor.isSystemFn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KQSql;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;

public class KUnresolvedIdsAnnotator implements Annotator {

  private static final String[] IMPLICIT_VARS = new String[] {"x", "y", "z"};

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof KUserId)) {
      return;
    }
    final KUserId usage = (KUserId)element;
    if (usage.isDeclaration()) {
      return;
    }
    final PsiElement declaration = findDeclaration(usage);
    if (declaration != null) {
      return;
    }
    final String variableName = usage.getName();
    if (isSystemFn(variableName)) {
      return;
    }
    // check if it's one of the implicit x,y,z parameters
    for (String implicitVar : IMPLICIT_VARS) {
      if (implicitVar.equals(variableName)) {
        final KLambda enclosingLambda = PsiTreeUtil.getContextOfType(usage, KLambda.class);
        if (enclosingLambda != null && enclosingLambda.getLambdaParams() == null) {
          return;
        }
      }
    }
    final KQSql enclosingQSql = PsiTreeUtil.getContextOfType(usage, KQSql.class);
    if (enclosingQSql != null) {
      return; // ignore every non-resolved variable as it may be referencing a column name
    }
    holder.createWeakWarningAnnotation(usage, String.format("`%s` might not have been defined", variableName));
  }

  @Nullable
  private PsiElement findDeclaration(KUserId usage) {
    final PsiReference[] references = usage.getReferences();
    if (references.length == 0) {
      return null;
    }
    return references[0].resolve();
  }

}
