package com.appian.intellij.k;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameTest extends LightCodeInsightFixtureTestCase {

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    for (String context : new String[]{"top", "fn"}) {
      for (String fromType : new String[]{"local"}) {
        for (String toType : new String[]{"local", "global"}) {
          for (String from : new String[]{"def", "usage"}) {
            final String fileNamePrefix = context + "_" + fromType + "_to_" + toType + "_from_" + from;
            final String renameTo = "local".equals(toType) ? "test2" : ".g.test";
            suite.addTest(new RenameTest(fileNamePrefix, renameTo));
          }
        }
      }
    }
    return suite;
  }

  private final String fileNamePrefix;
  private final String targetName;

  public RenameTest(String fileNamePrefix, String targetName) {
    this.fileNamePrefix = fileNamePrefix;
    this.targetName = targetName;
    setName("testRename");
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/" + getClass().getName().replace('.', '/');
  }

  @Override
  public String getName() {
    return fileNamePrefix + ".k";
  }

  public void testRename() {
    final String inputFileName = fileNamePrefix + ".k";
    final String outputFileName = fileNamePrefix + "_after.k";
    myFixture.configureByFile(inputFileName);
    final PsiFile virtualFile = myFixture.getFile();
    assertReferences(virtualFile);
    myFixture.renameElementAtCaret(targetName);
    myFixture.checkResultByFile(outputFileName, false);
  }

  private void assertReferences(PsiFile file) {
    final PsiElement elementAt = file.findElementAt(myFixture.getCaretOffset());
    final PsiElement userIdElement = elementAt.getContext();
    assertInstanceOf(userIdElement, KUserId.class);
    final KUserId userId = (KUserId)userIdElement;
    final PsiReference[] references = userId.getReferences();
    assertEquals(1, references.length);
    final PsiElement resolved = references[0].resolve();
    if (userId.isDeclaration()) {
      assertNull("Declaration should not resolve to itself",resolved);
    } else {
      assertNotNull("Declaration not found",resolved);
    }
  }

}
