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
    testCompletion(new String[] {"globals1.k", "globals2.k"}, "square", "sum",
        "someFnInDefaultNsInAnotherFile", "save", "scan", "scov", "sdev", "select", "set", "setenv", "show",
        "signum", "sin", "sqrt", "ss", "ssr", "string", "sublist", "sum", "sums", "sv", "svar", "system");
  }

  public void testSystemFns_k_k() {
    testCompletion("system_fns_k.k", "bin", "binr", "by");
  }
  public void testSystemFns_k_q() {
    testCompletion("system_fns_k.q", "bin", "binr", "by");
  }
  public void testSystemFns_q_q() {
    testCompletion("system_fns_q.q", "cols", "cor", "cos", "count", "cov");
  }
  public void testSystemFns_q_k() {
    testCompletion("system_fns_q.k", "cols", "cor", "cos", "count", "cov");
  }

  private void testCompletion(String fileName, String... expectedSuggestions) {
    testCompletion(new String[]{fileName}, expectedSuggestions);
  }

  private void testCompletion(String[] fileNames, String... expectedSuggestions) {
    myFixture.configureByFiles(fileNames);
    myFixture.complete(CompletionType.BASIC, 1);
    final List<String> strings = myFixture.getLookupElementStrings();
    assertOrderedEquals(strings, expectedSuggestions);
  }

}
