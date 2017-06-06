package com.appian.intellij.k;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

public class QParserTest extends KParserTest {

  static final String TEST_DATA_FOLDERS_PATH =
      "src/test/resources/" + QParserTest.class.getName().replace('.', '/');

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    final File folder = new File(TEST_DATA_FOLDERS_PATH);
    final String[] fileNames = folder.list();
    if (fileNames == null) {
      throw new RuntimeException("Folder not found: " + folder);
    }
    for(String fileName : fileNames) {
      if (fileName.startsWith("_")) { // e.g. sandboxØ
        continue;
      }
      suite.addTest(new QParserTest(fileName));
    }
    return suite;
  }

  QParserTest(String testFileName) {
    super(testFileName, "q");
  }

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_FOLDERS_PATH;
  }

}
