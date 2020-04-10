package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KQSql;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;

public final class KRainbowVisitor extends RainbowVisitor {

  @Override
  public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof KFile;
  }

  @Override
  public void visit(@NotNull PsiElement element) {
    if (!(element instanceof KUserId)) {
      return;
    }
    PsiElement context = PsiTreeUtil.findFirstParent(element, p -> p instanceof KLambda);
    if (context == null) {
      return; // don't include globals
    }
    final KUserId usage = (KUserId)element;
    if (KUtil.isNamespacedId(usage.getText())) {
      return; // don't include globals
    }
    PsiElement rainbowElement = usage.getNameIdentifier();
    PsiElement declaration;
    if (usage.isDeclaration()) {
      declaration = usage;
    } else {
      declaration = findDeclaration(usage);
      PsiElement resolvedContext = PsiTreeUtil.findFirstParent(declaration, p -> p instanceof KLambda);
      if (resolvedContext == null) {
        return; // don't include globals
      }
    }
    final KQSql enclosingQSql = PsiTreeUtil.getContextOfType(usage, KQSql.class);
    if (enclosingQSql != null) {
      return; // don't highlight column selections
    }
    HighlightInfo attrs = getRainbowSymbolKey(context, rainbowElement, declaration);
    addInfo(attrs);
  }

  @Nullable
  private HighlightInfo getRainbowSymbolKey(
      @NotNull PsiElement context, PsiElement rainbowElement, PsiElement resolved) {
    if (rainbowElement == null || resolved == null) {
      return null;
    }
    if (rainbowElement == resolved && KUnusedVariableHighlightVisitor.isUnusedKLocalDeclaration(resolved)) {
      // gray out unused variables
      return HighlightInfo.newHighlightInfo(HighlightInfoType.UNUSED_SYMBOL)
          .range(resolved)
          .textAttributes(CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES)
          .create();
    }
    final String name = ((KUserId)resolved).getName();
    return getInfo(context, rainbowElement, name, KSyntaxHighlighter.IDENTIFIER);
  }

  @Nullable
  private PsiElement findDeclaration(KUserId usage) {
    final PsiReference[] references = usage.getReferences();
    if (references.length == 0) {
      return null;
    }
    return references[0].resolve();
  }

  @NotNull
  @Override
  public HighlightVisitor clone() {
    return new KRainbowVisitor();
  }

}
