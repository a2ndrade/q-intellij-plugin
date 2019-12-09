package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameUnderNamespaceTest extends RenameUnderRootTest {

  public RenameUnderNamespaceTest(int totalCarets, int currentCaret) {
    super(totalCarets, currentCaret);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    int total = 12;
    for (int i = 1; i <= total; i++) {
      suite.addTest(new RenameUnderNamespaceTest(total, i));
    }
    return suite;
  }

  @Override
  boolean isExpectedHighlighting(String newName, int currentCaret) {
    switch (currentCaret) {
    case 6:
    case 7:
      return "ns.global".equals(newName);
    }
    return false;
  }
}
