package com.appian.intellij.k;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;

abstract class RenameTestBase extends LightPlatformCodeInsightFixture4TestCase {

  private final int totalCarets;
  private final int currentCaret;

  RenameTestBase(int totalCarets, int currentCaret) {
    this.totalCarets = totalCarets;
    this.currentCaret = currentCaret;
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/" + getClass().getName().replace('.', '/');
  }

  @Override
  public String getName() {
    return String.format("<caret#%d>", currentCaret);
  }

  @Test
  public void testRename() throws IOException {
    doRename("global2");
    doRename("ns.global");
    doRename(".ns.global");
  }

  private void doRename(String newName) throws IOException {
    final String template = readTemplate();
    myFixture.configureByText("template.q", template);
    myFixture.checkHighlighting(true, true, true);
    myFixture.renameElementAtCaret(newName);
    final String expectedFileName = getExpectedFileName(currentCaret);
    final String outputTemplate = readFile(expectedFileName);
    final String output = outputTemplate.replaceAll("<new-name>", newName);
    System.out.println(output);
    System.out.println("************************************************");
    myFixture.checkResult(output, false);
    boolean expectedHighlighting = isExpectedHighlighting(newName, currentCaret);
    try {
      myFixture.checkHighlighting(true, true, true);
    } catch (Throwable e) {
      if (expectedHighlighting) {
        return;
      }
      throw e;
    }
    assertFalse("Expected highlighting error", expectedHighlighting);
  }

  abstract String getExpectedFileName(int currentCaret);

  boolean isExpectedHighlighting(String newName, int currentCaret) {
    return false;
  }

  private String readTemplate() throws IOException {
    String template = readFile("template.q");
    for (int i = 1; i <= totalCarets; i++) {
      if (i == currentCaret) {
        template = template.replace(marker(i), "<caret>");
        continue;
      }
      template = template.replace(marker(i), "");
    }
    return template;
  }

  @NotNull
  private String readFile(String testFileName) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(getTestDataPath() + "/" + testFileName));
    return new String(encoded, StandardCharsets.UTF_8);
  }

  private String marker(int caretNumber) {
    return String.format("<caret#%d>", caretNumber);
  }

}
