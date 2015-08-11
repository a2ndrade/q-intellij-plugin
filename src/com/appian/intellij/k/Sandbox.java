package com.appian.intellij.k;

import com.intellij.psi.tree.IElementType;

public final class Sandbox {
  public static void main(String[] args) throws Exception {
    final String input = "$ / some _comment\n* 1 2 0N -4 34 0I\n10.86 +/ LIST \" something\n \\\" is in the !rain!!@\"";
    final KLexer lexer = new KLexer(null);
    lexer.reset(input, 0, input.length(), KLexer.YYINITIAL);
    IElementType elementType = null;
    do {
      elementType = lexer.advance();
      if (elementType != null) {
        System.out.println(elementType);
      } else {
        System.out.println("Done.");
      }
    } while (elementType != null);
  }
}
