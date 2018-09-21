package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
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
    final KUserId identifier = (KUserId)element;
    if (identifier.getName().indexOf('.') != -1) {
      return; // don't include globals
    }
    PsiElement rainbowElement = identifier.getNameIdentifier();
    PsiElement resolved;
    if (identifier.isDeclaration()) {
      resolved = identifier;
    } else {
      resolved = findDeclaration(identifier);
      PsiElement resolvedContext = PsiTreeUtil.findFirstParent(resolved, p -> p instanceof KLambda);
      if (resolvedContext == null) {
        return; // don't include globals
      }
    }
    HighlightInfo attrs = getRainbowSymbolKey(context, rainbowElement, resolved);
    addInfo(attrs);
  }

  @Nullable
  private HighlightInfo getRainbowSymbolKey(@NotNull PsiElement context, PsiElement rainbowElement, PsiElement resolved) {
    if (rainbowElement == null || resolved == null) {
      return null;
    }
    String name = ((KUserId)resolved).getName();
    if (name != null) {
      return getInfo(context, rainbowElement, name, KSyntaxHighlighter.IDENTIFIER);
    }
    return null;
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
