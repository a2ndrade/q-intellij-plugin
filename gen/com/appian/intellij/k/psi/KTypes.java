// This is a generated file. Not intended for manual editing.
package com.appian.intellij.k.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.appian.intellij.k.psi.impl.*;

public interface KTypes {

  IElementType VECTOR = new KElementType("VECTOR");

  IElementType AMPERSAND = new KTokenType("&");
  IElementType ASTERISK = new KTokenType("*");
  IElementType AT = new KTokenType("@");
  IElementType BACK_SLASH = new KTokenType("\\");
  IElementType BACK_SLASH_COLON = new KTokenType("\\:");
  IElementType BACK_TICK = new KTokenType("`");
  IElementType BANG = new KTokenType("!");
  IElementType CARET = new KTokenType("^");
  IElementType CLOSE_BRACE = new KTokenType("}");
  IElementType CLOSE_BRACET = new KTokenType("]");
  IElementType CLOSE_PAREN = new KTokenType(")");
  IElementType COLON = new KTokenType(":");
  IElementType COMMA = new KTokenType(",");
  IElementType COMMENT = new KTokenType("comment");
  IElementType DASH = new KTokenType("-");
  IElementType DO = new KTokenType("do");
  IElementType DOLLAR = new KTokenType("$");
  IElementType EQUALS = new KTokenType("=");
  IElementType FIVECOLON = new KTokenType("5:");
  IElementType FOURCOLON = new KTokenType("4:");
  IElementType GREATER_THAN = new KTokenType(">");
  IElementType HASH = new KTokenType("#");
  IElementType IDENTIFIER = new KTokenType("identifier");
  IElementType IF = new KTokenType("if");
  IElementType LESS_THAN = new KTokenType("<");
  IElementType NIL = new KTokenType("_n");
  IElementType NUMBER = new KTokenType("number");
  IElementType ONECOLON = new KTokenType("1:");
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
  IElementType SIXCOLON = new KTokenType("6:");
  IElementType SLASH = new KTokenType("/");
  IElementType SLASH_COLON = new KTokenType("/:");
  IElementType STRING = new KTokenType("string");
  IElementType SYMBOL = new KTokenType("symbol");
  IElementType SYSFUNCTION = new KTokenType("sysFunction");
  IElementType THREECOLON = new KTokenType("3:");
  IElementType TICK = new KTokenType("'");
  IElementType TICK_COLON = new KTokenType("':");
  IElementType TILDE = new KTokenType("~");
  IElementType TWOCOLON = new KTokenType("2:");
  IElementType UNDERSCORE = new KTokenType("_");
  IElementType WHILE = new KTokenType("while");
  IElementType ZEROCOLON = new KTokenType("0:");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == VECTOR) {
        return new KVectorImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
