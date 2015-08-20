package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

public class QuickRunTest extends KParserTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new KParserTest("@QuickRunTest.txt"));
    return suite;
  }

  QuickRunTest(String testFileName) {
    super(testFileName);
  }


}
