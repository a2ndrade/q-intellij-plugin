package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameUnderNamespace_ManyDeclarationsTest extends RenameUnderRoot_ManyDeclarationsTest {

  public RenameUnderNamespace_ManyDeclarationsTest(int totalCarets, int currentCaret) {
    super(totalCarets, currentCaret);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    final int total = 18;
    for (int i = 1; i <= total; i++) {
      suite.addTest(new RenameUnderNamespace_ManyDeclarationsTest(total, i));
    }
    return suite;
  }

}
