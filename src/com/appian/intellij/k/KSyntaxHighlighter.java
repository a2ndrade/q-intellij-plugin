package com.appian.intellij.k;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import java.awt.*;
import java.io.Reader;
import java.util.Map;

import com.appian.intellij.k.psi.KTypes;
import com.google.common.collect.ImmutableMap;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

public final class KSyntaxHighlighter extends SyntaxHighlighterBase {

  public static final TextAttributesKey OPERATOR = createTextAttributesKey("K_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("K_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey SYMBOL = createTextAttributesKey("K_SYMBOL", DefaultLanguageHighlighterColors.CONSTANT);
  public static final TextAttributesKey NUMBER = createTextAttributesKey("K_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey KEYWORD = createTextAttributesKey("K_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey SYSFUNCTION = createTextAttributesKey("K_SYSFUNCTION", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey STRING = createTextAttributesKey("K_STRING", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey COMMENT = createTextAttributesKey("K_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

  static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("K_BAD_CHARACTER",
    new TextAttributes(Color.RED, null, null, null, Font.BOLD));

  private static final TextAttributesKey[] OPERATOR_KEYS = new TextAttributesKey[]{OPERATOR};
  private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{IDENTIFIER};
  private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
  private static final TextAttributesKey[] SYMBOL_KEYS = new TextAttributesKey[]{SYMBOL};
  private static final TextAttributesKey[] SYSFUNCTION_KEYS = new TextAttributesKey[]{SYSFUNCTION};
  private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
  private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
  private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};

  private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
  private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};

  @Override
  public Lexer getHighlightingLexer() {
    return new FlexAdapter(new KLexer((Reader)null));
  }

  private static final Map<IElementType, TextAttributesKey[]> M = ImmutableMap.<IElementType, TextAttributesKey[]>builder()
    .put(KTypes.IDENTIFIER, IDENTIFIER_KEYS)
    .put(KTypes.SYSFUNCTION, SYSFUNCTION_KEYS)
    .put(KTypes.NUMBER, NUMBER_KEYS)
    .put(KTypes.STRING, STRING_KEYS)
    .put(KTypes.SYMBOL, SYMBOL_KEYS)

    .put(KTypes.ASTERISK, OPERATOR_KEYS)
    .put(KTypes.AT, OPERATOR_KEYS)
    .put(KTypes.BACK_SLASH, OPERATOR_KEYS)
    .put(KTypes.BACK_SLASH_COLON, OPERATOR_KEYS)

    .put(KTypes.COLON, KEYWORD_KEYS)
    .put(KTypes.IF, KEYWORD_KEYS)
    .put(KTypes.DO, KEYWORD_KEYS)
    .put(KTypes.WHILE, KEYWORD_KEYS)

    .put(KTypes.COMMENT, COMMENT_KEYS)
    .put(TokenType.BAD_CHARACTER, BAD_CHAR_KEYS)

    .build();

  @Override
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    final TextAttributesKey[] keys = M.get(tokenType);
    if (keys != null) {
      return keys;
    }
    return EMPTY_KEYS;
  }

}
