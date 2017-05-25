package com.appian.intellij.k;

import java.util.Arrays;
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
    return "test-data";
  }

  public void testCompletion() {
    myFixture.configureByFiles("CompletionTest.k");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertTrue(strings.containsAll(Arrays.asList("sum", "power", "product", "square")));
    assertEquals(4, strings.size());
  }

}
