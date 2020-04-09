package com.appian.intellij.k;

import static com.appian.intellij.k.KCompletionContributor.isSystemFn;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KNamedElement;
import com.appian.intellij.k.psi.KQSql;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;

public class KUnresolvedIdsAnnotator implements Annotator {

  private static final String Q_INTELLIJ_PLUGIN_CONFIG = ".q-intellij-plugin";

  private enum LinterPreset {
    NONE, APPIAN
  }

  private static LinterPreset linterPreset;

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
      checkInternalReferenceViolation(usage, declaration, holder);
      return;
    }
    final String variableName = usage.getName();
    if (isSystemFn(variableName)) {
      return;
    }
    // check if it's one of the implicit x,y,z parameters
    for (String implicitVar : KNamedElement.IMPLICIT_VARS) {
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

  private void checkInternalReferenceViolation(KUserId usage, PsiElement declaration, AnnotationHolder holder) {
    final LinterPreset linter = getLinterPreset(usage);
    if (linter != LinterPreset.APPIAN) {
      return;
    }
    boolean isInternal =
        usage.isInternal() && !usage.getContainingFile().isEquivalentTo(declaration.getContainingFile());
    if (isInternal) {
      holder.createErrorAnnotation(usage,
          String.format("`%s` is an internal function. It should only be accessed within %s", usage.getName(),
              declaration.getContainingFile().getVirtualFile().getName()));
    }
  }

  private LinterPreset getLinterPreset(KUserId usage) {
    if (linterPreset != null) {
      return linterPreset;
    }
    return linterPreset = Optional.of(usage)
        .map(PsiElement::getProject)
        .map(ProjectUtil::guessProjectDir)
        .map(p -> p.findChild(Q_INTELLIJ_PLUGIN_CONFIG))
        .map(configFile -> {
          final Properties props = new Properties();
          try {
            props.load(configFile.getInputStream());
            if ("appian".equals(props.getProperty("preset"))) {
              return LinterPreset.APPIAN;
            }
          } catch (IOException ignore) {
          }
          return LinterPreset.NONE;
        })
        .orElse(LinterPreset.NONE);
  }

  @Nullable
  private PsiElement findDeclaration(KUserId usage) {
    final PsiReference[] references = usage.getReferences();
    if (references.length == 0) {
      return null;
    }
    final PsiReference reference = references[0];
    if (reference instanceof KReference) {
      return ((KReference)reference).resolveFirstUnordered();
    }
    return reference.resolve();
  }

}
