// This is a generated file. Not intended for manual editing.
package com.appian.intellij.k.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.appian.intellij.k.psi.KTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class KParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == VECTOR) {
      r = vector(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  /* ********************************************************** */
  // comment
  //             | symbol
  //             | vector
  //             | string
  //             | number
  //             | identifier
  //             | sysFunction
  //             | '[' E ']'
  //             | '{' E '}'
  //             | '(' E ')'
  //             | ';' E
  static boolean E(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "E")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, SYMBOL);
    if (!r) r = vector(b, l + 1);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, SYSFUNCTION);
    if (!r) r = E_7(b, l + 1);
    if (!r) r = E_8(b, l + 1);
    if (!r) r = E_9(b, l + 1);
    if (!r) r = E_10(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '[' E ']'
  private static boolean E_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "E_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACKET);
    r = r && E(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACET);
    exit_section_(b, m, null, r);
    return r;
  }

  // '{' E '}'
  private static boolean E_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "E_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACE);
    r = r && E(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' E ')'
  private static boolean E_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "E_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_PAREN);
    r = r && E(b, l + 1);
    r = r && consumeToken(b, CLOSE_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';' E
  private static boolean E_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "E_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && E(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // E+ <<eof>>
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_0(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // E+
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = E(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!E(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ',' number | number number+
  public static boolean vector(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vector")) return false;
    if (!nextTokenIs(b, "<vector>", COMMA, NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<vector>");
    r = vector_0(b, l + 1);
    if (!r) r = vector_1(b, l + 1);
    exit_section_(b, l, m, VECTOR, r, false, null);
    return r;
  }

  // ',' number
  private static boolean vector_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vector_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && consumeToken(b, NUMBER);
    exit_section_(b, m, null, r);
    return r;
  }

  // number number+
  private static boolean vector_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vector_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER);
    r = r && vector_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // number+
  private static boolean vector_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vector_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, NUMBER)) break;
      if (!empty_element_parsed_guard_(b, "vector_1_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

}
