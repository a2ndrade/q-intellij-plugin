package com.appian.intellij.k;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import java.awt.Color;
import java.awt.Font;
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

  public static final TextAttributesKey VERB = createTextAttributesKey("K_OPERATOR", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey ADVERB = createTextAttributesKey("K_ADVERB", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("K_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey IDENTIFIER_SYS = createTextAttributesKey("K_SYSFUNCTION", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey SYMBOL = createTextAttributesKey("K_SYMBOL", DefaultLanguageHighlighterColors.CONSTANT);
  public static final TextAttributesKey NUMBER = createTextAttributesKey("K_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey KEYWORD = createTextAttributesKey("K_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey STRING = createTextAttributesKey("K_STRING", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey BRACES = createTextAttributesKey("K_BRACES", DefaultLanguageHighlighterColors.BRACES);
  public static final TextAttributesKey BRACKETS = createTextAttributesKey("K_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
  public static final TextAttributesKey PARENS = createTextAttributesKey("K_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
  public static final TextAttributesKey COMMENT = createTextAttributesKey("K_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

  static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("K_BAD_CHARACTER",
    new TextAttributes(Color.RED, null, null, null, Font.BOLD));

  private static final TextAttributesKey[] VERB_KEYS = new TextAttributesKey[]{VERB};
  private static final TextAttributesKey[] ADVERB_KEYS = new TextAttributesKey[]{ADVERB};
  private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{IDENTIFIER};
  private static final TextAttributesKey[] IDENTIFIER_SYS_KEYS = new TextAttributesKey[]{IDENTIFIER_SYS};
  private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
  private static final TextAttributesKey[] SYMBOL_KEYS = new TextAttributesKey[]{SYMBOL};
  private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
  private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
  private static final TextAttributesKey[] BRACES_KEYS = new TextAttributesKey[]{BRACES};
  private static final TextAttributesKey[] BRACKETS_KEYS = new TextAttributesKey[]{BRACKETS};
  private static final TextAttributesKey[] PARENS_KEYS = new TextAttributesKey[]{PARENS};
  private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};

  private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
  private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};

  @Override
  public Lexer getHighlightingLexer() {
    return new FlexAdapter(new KLexer((Reader)null));
  }

  private static final Map<IElementType, TextAttributesKey[]> M = ImmutableMap.<IElementType, TextAttributesKey[]>builder()

    .put(KTypes.USER_IDENTIFIER, IDENTIFIER_KEYS)
    .put(KTypes.K3_SYSTEM_FUNCTION, IDENTIFIER_SYS_KEYS)
    .put(KTypes.Q_SYSTEM_FUNCTION, IDENTIFIER_SYS_KEYS)

    .put(KTypes.NUMBER, NUMBER_KEYS)
    .put(KTypes.NUMBER_VECTOR, NUMBER_KEYS)

    .put(KTypes.CHAR, STRING_KEYS)
    .put(KTypes.STRING, STRING_KEYS)

    .put(KTypes.SYMBOL, SYMBOL_KEYS)
    .put(KTypes.SYMBOL_VECTOR, SYMBOL_KEYS)

    .put(KTypes.VERB, VERB_KEYS)
    .put(KTypes.ADVERB, ADVERB_KEYS)

    .put(KTypes.COMMAND, IDENTIFIER_SYS_KEYS)
    .put(KTypes.CURRENT_NAMESPACE, IDENTIFIER_SYS_KEYS)

    .put(KTypes.COLON, KEYWORD_KEYS)
    .put(KTypes.CONTROL, KEYWORD_KEYS)
    .put(KTypes.CONDITIONAL, KEYWORD_KEYS)

    .put(KTypes.OPEN_BRACE, BRACES_KEYS)
    .put(KTypes.CLOSE_BRACE, BRACES_KEYS)
    .put(KTypes.OPEN_BRACKET, BRACKETS_KEYS)
    .put(KTypes.CLOSE_BRACKET, BRACKETS_KEYS)
    .put(KTypes.OPEN_PAREN, PARENS_KEYS)
    .put(KTypes.CLOSE_PAREN, PARENS_KEYS)

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
