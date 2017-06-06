package com.appian.intellij.k;

import java.util.List;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class CompletionTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/" + getClass().getName().replace('.', '/');
  }

  public void testGlobals() {
    testCompletion("globals.k", "square", "sum"); // none expected
  }

  public void testSystemFns_k_k() {
    testCompletion("system_fns_k.k", "_bd", "_bin", "_binl");
  }
  public void testSystemFns_k_q() {
    testCompletion("system_fns_k.q"); // none expected
  }
  public void testSystemFns_q_q() {
    testCompletion("system_fns_q.q", "cols", "cor", "cos", "count", "cov");
  }
  public void testSystemFns_q_k() {
    testCompletion("system_fns_q.k"); // none expected
  }

  private void testCompletion(String fileName, String... expectedSuggestions) {
    myFixture.configureByFiles(fileName);
    myFixture.complete(CompletionType.BASIC, 1);
    final List<String> strings = myFixture.getLookupElementStrings();
    assertOrderedEquals(strings, expectedSuggestions);
  }

}
