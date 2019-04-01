package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KFile;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public class KTypedHandler extends TypedHandlerDelegate {

  @NotNull
  @Override
  public Result charTyped(
      char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (!(file instanceof KFile)) {
      return Result.CONTINUE;
    }
    if (isInsideStringLiteral(editor, file)) {
      return Result.CONTINUE;
    }
    if (c == '[' || c == ';') {
      AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, null);
    }
    return Result.CONTINUE;
  }

  private static boolean isInsideStringLiteral(@NotNull Editor editor, @NotNull PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return false;
    }
    final ParserDefinition definition = LanguageParserDefinitions.INSTANCE.forLanguage(element.getLanguage());
    if (definition != null) {
      final TokenSet stringLiteralElements = definition.getStringLiteralElements();
      final ASTNode node = element.getNode();
      if (node == null) {
        return false;
      }
      final IElementType elementType = node.getElementType();
      if (stringLiteralElements.contains(elementType)) {
        return true;
      }
      PsiElement parent = element.getParent();
      if (parent != null) {
        ASTNode parentNode = parent.getNode();
        if (parentNode != null && stringLiteralElements.contains(parentNode.getElementType())) {
          return true;
        }
      }
    }
    return false;
  }
}
