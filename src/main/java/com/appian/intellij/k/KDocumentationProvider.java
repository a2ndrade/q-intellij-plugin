package com.appian.intellij.k;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KExpression;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

public final class KDocumentationProvider extends AbstractDocumentationProvider {

  @Nullable
  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    if (!isDeclaration(element)) {
      return null;
    }
    final KUserId userId = (KUserId)element;
    return getFunctionSignature(userId).orElse(null);
  }

  @Override
  public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
    if (!isDeclaration(element)) {
      return null;
    }
    final KUserId userId = (KUserId)element;
    return getComments(userId).map(
        comments -> String.join("\n<br>", comments, getFunctionSignature(userId).orElse(""))).orElse(null);
  }

  @Nullable
  private boolean isDeclaration(PsiElement element) {
    if (!(element instanceof KUserId)) {
      return false;
    }
    final KUserId userId = (KUserId)element;
    if (userId.isDeclaration()) {
      return true;
    }
    return false;
  }

  private Optional<String> getFunctionSignature(KUserId userId) {
    return Optional.of(userId)
        .map(KUserId::getParent)
        .map(KAssignment.class::cast)
        .map(KAssignment::getExpression)
        .map(KExpression::getLambdaList)
        .filter(l -> !l.isEmpty())
        .map(l -> l.get(0))
        .map(l -> {
          final KLambdaParams lambdaParams = l.getLambdaParams();
          return lambdaParams == null ? Collections.<KUserId>emptyList() : lambdaParams.getUserIdList();
        })
        .map(Collection::stream).map(s -> {
          final List<String> paramNames = s.map(KUserId::getName).collect(Collectors.toList());
          return String.format("%s[%s] - %s", userId.getName(), String.join(";", paramNames),
              userId.getContainingFile().getName());
        });
  }

  @Nullable
  private Optional<String> getComments(KUserId userId) {
    final KExpression fnDeclaration = PsiTreeUtil.getContextOfType(userId, KExpression.class);
    final Deque<String> comments = new ArrayDeque<>();
    PsiElement curr = fnDeclaration.getPrevSibling();
    while (curr instanceof PsiComment) {
      final PsiComment comment = (PsiComment)curr;
      comments.push(comment.getText());
      curr = curr.getPrevSibling();
    }
    if (comments.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(String.join("\n<br>", comments));
  }

}
