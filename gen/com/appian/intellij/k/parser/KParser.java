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
    if (t == ROOT) {
      r = root_(b, 0);
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
  // root_
  static boolean root(PsiBuilder b, int l) {
    return root_(b, l + 1);
  }

  /* ********************************************************** */
  // COMMENT
  public static boolean root_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_")) return false;
    if (!nextTokenIs(b, COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMENT);
    exit_section_(b, m, ROOT, r);
    return r;
  }

}
