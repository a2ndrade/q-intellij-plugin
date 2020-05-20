package com.appian.intellij.k;

import static org.junit.Assert.assertArrayEquals;

import java.util.List;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class HighlightingTest extends BasePlatformTestCase {
  public void testUnusedArg() {
    testHighlighting("f:{[t] 1+2 }", "t");
  }

  public void testUnusedLocal() {
    testHighlighting("f:{[t] a:1+2; t*2 }", "a");
  }

  public void testQSqlColumnDeclaration() {
    // column c should not be highlighted as unused
    testHighlighting("f: {[t] update c:c1%c2 from t }");
  }

  public void testLiteralColumnDeclaration() {
    // column c should not be highlighted as unused
    testHighlighting("f: {a:([] c: 1 2 3); select from a}");
  }

  private void testHighlighting(String text, String... expected) {
    myFixture.configureByText("dummy.q", text);
    List<HighlightInfo> highlightInfos = myFixture.doHighlighting();

    String[] highlights = highlightInfos.stream().map(HighlightInfo::getText).toArray(String[]::new);

    assertArrayEquals(expected, highlights);
  }
}
