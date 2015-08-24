package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.io.PrintStream;

public class GeneratorTest extends KParserTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    final File folder = new File(TEST_DATA_FOLDER_NAME);
    for(String fileName : folder.list()) {
      suite.addTest(new GeneratorTest(fileName));
    }
    return suite;
  }

  GeneratorTest(String testFileName) {
    super(testFileName);
    setName("testGenerateTestCases");
  }

  public void testGenerateTestCases() throws Exception {
    final File input = new File(getTestDataPath() + "/" + testFileName);
    final String[] testCases = readFileIntoSections(input, TEST_CASE_SEPARATOR);
    final File outputDir = new File("/Users/antonio.andrade/ae/");
    final File outputFile = new File(outputDir, testFileName);
    final PrintStream ps = new PrintStream(outputFile);
    for (int i = 0; i < testCases.length; i++) {
      final String testCase = testCases[i];
      if (testCase.isEmpty()) {
        continue;
      }
      final String[] inOut = readFileIntoSections(testCase, INPUT_OUTPUT_SEPARATOR);
      final String expression = inOut[0].trim();
      final String actual = parseAsString(expression).trim();
      ps.println(expression);
      ps.println(INPUT_OUTPUT_SEPARATOR);
      ps.println(actual);
      if (i < testCases.length - 1) {
        ps.println(TEST_CASE_SEPARATOR);
        ps.println();
        ps.println();
      }
    }
    ps.close();
  }


}
