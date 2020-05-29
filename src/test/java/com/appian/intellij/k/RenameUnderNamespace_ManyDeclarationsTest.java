package com.appian.intellij.k;

import static org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RenameUnderNamespace_ManyDeclarationsTest extends RenameUnderRoot_ManyDeclarationsTest {

  public RenameUnderNamespace_ManyDeclarationsTest(int totalCarets, int currentCaret) {
    super(totalCarets, currentCaret);
  }

  @Parameters(name = "<caret#{1}")
  public static Collection<Object[]> suite() {
    final List<Object[]> testData = new ArrayList<>();
    final int total = 18;
    for (int i = 1; i <= total; i++) {
      testData.add(new Object[]{total, i});
    }
    return testData;
  }

}
