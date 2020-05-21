package com.appian.intellij.k;

import static org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RenameUnderRoot_ManyDeclarationsTest extends RenameTestBase {

  public RenameUnderRoot_ManyDeclarationsTest(int totalCarets, int currentCaret) {
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

  @Override
  String getExpectedFileName(int currentCaret) {
    switch (currentCaret) {
    case 1:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
    case 14:
    case 15:
    case 16:
    case 17:
    case 18:
      return "after_1.q";
    case 2:
    case 3:
    case 4:
      return "after_2.q";
    case 10:
    case 11:
    case 12:
    case 13:
      return "after_3.q";
    }
    throw new IllegalStateException("Missing mapping for caret#" + currentCaret);
  }

}
