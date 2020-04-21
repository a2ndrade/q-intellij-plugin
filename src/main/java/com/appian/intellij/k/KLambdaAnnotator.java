package com.appian.intellij.k;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;

final class KLambdaAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof KLambdaParams)) {
      return;
    }
    final List<KUserId> params = ((KLambdaParams)element).getUserIdList();
    if (params.size() > 8) {
      holder.createErrorAnnotation(element, "Too many parameters (8 max)");
    }
    final Map<String, KUserId> names = new HashMap<>();
    for (KUserId param : params) {
      KUserId existing = names.get(param.getName());
      if (existing != null) {
        holder.createErrorAnnotation(existing, "Parameter already defined");
        holder.createErrorAnnotation(param, "Parameter already defined");
      }
      names.put(param.getName(), param);
    }
  }

}
