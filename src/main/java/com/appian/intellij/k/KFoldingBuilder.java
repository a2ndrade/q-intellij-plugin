package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;


public class KFoldingBuilder extends FoldingBuilderEx implements DumbAware {
  private static final int MAX_LOOKUP_DEPTH = 10;

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
    List<FoldingDescriptor> descriptors = new ArrayList<>();
    buildInnerFoldRegions(root, descriptors, 0);

    return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
  }

  private void buildInnerFoldRegions(PsiElement root, List<FoldingDescriptor> descriptors, int depth) {
    Stack<PsiElement> openBracketStack = new Stack<>();

    for (PsiElement child = root.getFirstChild(); child != null; child = PsiTreeUtil.skipWhitespacesForward(child)) {
      final IElementType elementType = PsiUtilCore.getElementType(child);

      if (isOpenBracket(elementType)) {
        openBracketStack.push(child);
      } else if (isCloseBracket(elementType) && !openBracketStack.empty()) {
        PsiElement matchingOpenBracket = openBracketStack.pop();
        addIfValidFoldingRegion(matchingOpenBracket, child, descriptors);
      }

      if (child.getChildren().length > 0 && depth < MAX_LOOKUP_DEPTH) {
        buildInnerFoldRegions(child, descriptors, depth + 1);
      }
    }
  }

  private boolean isOpenBracket(IElementType elementType) {
    return KTypes.OPEN_BRACE == elementType;
  }

  private boolean isCloseBracket(IElementType elementType) {
    return KTypes.CLOSE_BRACE == elementType;
  }

  /**
   * Checks if the text region between the startElement and endElement is a valid folding region,
   * and if valid, adds to the list of overall descriptors.
   *
   * This safeguards against cases where missing matching brackets result in incorrect folding region
   * calculations. In some cases, the endElement found may come before the given startElement in the file.
   */
  private void addIfValidFoldingRegion(PsiElement startElement, PsiElement endElement,
      List<FoldingDescriptor> descriptors) {
    int startOffset = startElement.getTextOffset() + startElement.getTextLength();
    int endOffset = endElement.getTextOffset();
    if (endOffset - startOffset > 0) {
      descriptors.add(new FoldingDescriptor(startElement, new TextRange(startOffset, endOffset)));
    }
  }

  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    return "...";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }
}
