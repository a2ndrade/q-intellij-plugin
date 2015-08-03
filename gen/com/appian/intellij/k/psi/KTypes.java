// This is a generated file. Not intended for manual editing.
package com.appian.intellij.k.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.appian.intellij.k.psi.impl.*;

public interface KTypes {

  IElementType PROPERTY = new KElementType("PROPERTY");

  IElementType COMMENT = new KTokenType("COMMENT");
  IElementType CRLF = new KTokenType("CRLF");
  IElementType KEY = new KTokenType("KEY");
  IElementType SEPARATOR = new KTokenType("SEPARATOR");
  IElementType VALUE = new KTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == PROPERTY) {
        return new KPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
