package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.appian.intellij.k.psi.KSymbol;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;

@RunWith(Parameterized.class)
public class RenameSymbolTest extends LightPlatformCodeInsightFixture4TestCase {

  @Parameterized.Parameters(name = "{0}.k")
  public static Collection<Object[]> suite() {
    final List<Object[]> testData = new ArrayList<>();
    for (String ns : new String[] {"", "other_ns_"}) {
      for (String toType : new String[] {"local", "global"}) {
        for (String from : new String[] {"def", "usage"}) {
          final String fileNamePrefix = ns + "local_to_" + toType + "_from_" + from;
          final String renameTo = "local".equals(toType) ? "test2" : ".g.test";
          testData.add(new Object[]{fileNamePrefix, renameTo, "usage".equals(from)});
        }
      }
    }
    return testData;
  }

  private final String fileNamePrefix;
  private final String targetName;
  private final boolean fromUsage;

  public RenameSymbolTest(String fileNamePrefix, String targetName, boolean fromUsage) {
    this.fileNamePrefix = fileNamePrefix;
    this.targetName = targetName;
    this.fromUsage = fromUsage;
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/" + getClass().getName().replace('.', '/');
  }

  @Test
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
    if (fromUsage) {
      assertInstanceOf(userIdElement, KSymbol.class);
    } else {
      assertInstanceOf(userIdElement, KUserId.class);
    }
    final PsiReference[] references = userIdElement.getReferences();
    assertEquals(1, references.length);
    final PsiElement resolved = references[0].resolve();
    if (fromUsage) {
      assertNotNull("Declaration not found", resolved);
    } else {
      assertNull("Declaration should not resolve to itself", resolved);
    }
  }

}
