package com.appian.intellij.k;

import com.appian.intellij.k.psi.KPrefixFnArgs;
import com.appian.intellij.k.psi.KPrefixFnCall;
import com.appian.intellij.k.psi.KTopLevelAssignment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class RenameTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return "test-data";
  }

  public void testReference() {
    myFixture.configureByFiles("RenameTest.k");
    doTestReference();
  }

  public void testReferenceNs() {
    myFixture.configureByFiles("RenameNsTest.k");
    doTestReference();
  }

  private void doTestReference() {
    PsiElement elementAt = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    KPrefixFnArgs element = (KPrefixFnArgs)elementAt.getParent();
    PsiReference[] references = ((KPrefixFnCall)element.getParent()).getPrefixFn()
        .getUserId()
        .getReferences();
    assertEquals(1, references.length);
    PsiElement topLevelAssignment = references[0].resolve().getParent();
    assertInstanceOf(topLevelAssignment, KTopLevelAssignment.class);
  }

  public void testRename() {
    myFixture.configureByFiles("RenameTest.k");
    myFixture.renameElementAtCaret("powerRenamed");
    myFixture.checkResultByFile("RenameTestAfter.k", false);
  }

  public void testRenameNs() {
    myFixture.configureByFiles("RenameNsTest.k");
    myFixture.renameElementAtCaret(".x.y.powerRenamed");
    myFixture.checkResultByFile("RenameNsTestAfter.k", false);
  }

}
