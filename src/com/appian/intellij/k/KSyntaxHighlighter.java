package com.appian.intellij.k;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import java.awt.*;
import java.io.Reader;

import com.appian.intellij.k.psi.KTypes;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

public final class KSyntaxHighlighter extends SyntaxHighlighterBase {

  public static final TextAttributesKey SEPARATOR = createTextAttributesKey("K_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey KEY = createTextAttributesKey("K_KEY", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey VALUE = createTextAttributesKey("K_VALUE", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey COMMENT = createTextAttributesKey("K_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

  static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("K_BAD_CHARACTER",
    new TextAttributes(Color.RED, null, null, null, Font.BOLD));

  private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
  private static final TextAttributesKey[] SEPARATOR_KEYS = new TextAttributesKey[]{SEPARATOR};
  private static final TextAttributesKey[] KEY_KEYS = new TextAttributesKey[]{KEY};
  private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
  private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
  private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

  @Override
  public Lexer getHighlightingLexer() {
    return new FlexAdapter(new KLexer((Reader)null));
  }

  @Override
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    if (tokenType.equals(KTypes.SEPARATOR)) {
      return SEPARATOR_KEYS;
    } else if (tokenType.equals(KTypes.KEY)) {
      return KEY_KEYS;
    } else if (tokenType.equals(KTypes.VALUE)) {
      return VALUE_KEYS;
    } else if (tokenType.equals(KTypes.COMMENT)) {
      return COMMENT_KEYS;
    } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
      return BAD_CHAR_KEYS;
    } else {
      return EMPTY_KEYS;
    }
  }

}
