package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SandboxTest extends KParserTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new KParserTest("_Sandbox.txt"));
    return suite;
  }

  SandboxTest(String testFileName) {
    super(testFileName);
  }


}
