package com.appian.intellij.k;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class RenameTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return "test-data/renaming";
  }

  public void testRenameAll() {
    for (String context : new String[]{"top", "fn"}) {
      for (String fromType : new String[]{"local"}) {
        for (String toType : new String[]{"global"}) {
          for (String from : new String[]{"def", "usage"}) {
            final String fileNamePrefix = context + "_" + fromType + "_to_" + toType + "_from_" + from;
            final String inputFileName = fileNamePrefix + ".k";
            final String outputFileName = fileNamePrefix + "_after.k";
            final String renameTo = "local".equals(toType) ? "test2" : ".g.test";
            myFixture.configureByFile(inputFileName);
            final PsiFile virtualFile = myFixture.getFile();
            assertReferences(virtualFile);
            myFixture.renameElementAtCaret(renameTo);
            myFixture.checkResultByFile(outputFileName, false);
          }
        }
      }
    }
  }

  private void assertReferences(PsiFile file) {
    final PsiElement userIdToken = file.findElementAt(myFixture.getCaretOffset());
    final PsiElement userId = userIdToken.getContext();
    assertInstanceOf(userId, KUserId.class);
    final PsiReference[] references = userId.getReferences();
    assertEquals(1, references.length);
    assertNotNull(references[0].resolve());
  }

}
