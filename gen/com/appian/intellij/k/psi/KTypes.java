// This is a generated file. Not intended for manual editing.
package com.appian.intellij.k.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.appian.intellij.k.psi.impl.*;

public interface KTypes {

  IElementType EMPTY = new KElementType("EMPTY");

  IElementType ALPHA = new KTokenType("alpha");
  IElementType AMPERSAND = new KTokenType("&");
  IElementType ASTERISK = new KTokenType("*");
  IElementType AT = new KTokenType("@");
  IElementType BACK_SLASH = new KTokenType("\\");
  IElementType BACK_TICK = new KTokenType("`");
  IElementType BANG = new KTokenType("!");
  IElementType CARET = new KTokenType("^");
  IElementType CLOSE_BRACE = new KTokenType("}");
  IElementType CLOSE_BRACET = new KTokenType("]");
  IElementType CLOSE_PAREN = new KTokenType(")");
  IElementType COLON = new KTokenType(":");
  IElementType COMMA = new KTokenType(",");
  IElementType COMMENT = new KTokenType("COMMENT");
  IElementType DASH = new KTokenType("-");
  IElementType DIGIT = new KTokenType("digit");
  IElementType DOLLAR = new KTokenType("$");
  IElementType EQUALS = new KTokenType("=");
  IElementType GREATER_THAN = new KTokenType(">");
  IElementType HASH = new KTokenType("#");
  IElementType LESS_THAN = new KTokenType("<");
  IElementType OPEN_BRACE = new KTokenType("{");
  IElementType OPEN_BRACKET = new KTokenType("[");
  IElementType OPEN_PAREN = new KTokenType("(");
  IElementType PERCENT = new KTokenType("%");
  IElementType PERIOD = new KTokenType(".");
  IElementType PIPE = new KTokenType("|");
  IElementType PLUS = new KTokenType("+");
  IElementType QUESTION_MARK = new KTokenType("?");
  IElementType QUOTE = new KTokenType("\"");
  IElementType SEMICOLON = new KTokenType(";");
  IElementType SLASH = new KTokenType("/");
  IElementType TICK = new KTokenType("'");
  IElementType TILDE = new KTokenType("~");
  IElementType UNDERSCORE = new KTokenType("_");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == EMPTY) {
        return new KEmptyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
