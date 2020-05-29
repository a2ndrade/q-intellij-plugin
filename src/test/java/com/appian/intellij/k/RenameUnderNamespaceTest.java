package com.appian.intellij.k;

import static org.junit.runners.Parameterized.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized;

public class RenameUnderNamespaceTest extends RenameUnderRootTest {

  public RenameUnderNamespaceTest(int totalCarets, int currentCaret) {
    super(totalCarets, currentCaret);
  }

  @Parameters(name = "<caret#{1}")
  public static Collection<Object[]> suite() {
    final List<Object[]> testData = new ArrayList<>();
    final int total = 12;
    for (int i = 1; i <= total; i++) {
      testData.add(new Object[]{total, i});
    }
    return testData;
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
