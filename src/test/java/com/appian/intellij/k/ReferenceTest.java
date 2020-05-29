package com.appian.intellij.k;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KUserId;
import com.google.common.collect.ImmutableMap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;

public class ReferenceTest extends LightPlatformCodeInsightFixture4TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/" + getClass().getName().replace('.', '/');
  }

  @Test
  public void testNoNs() {
    assertExactlyOneDeclarationFound("usage_no_ns.k", "noNs", "noNs");
  }

  @Test
  public void testUsageUnderNs() {
    assertNoDeclarationFound("usage_under_ns.k");
  }

  @Test
  public void testImplicitNs() {
    assertExactlyOneDeclarationFound("usage_implicit_ns.k", "implicitNs", ".a.b.implicitNs");
  }

  @Test
  public void testExplicitNs() {
    assertExactlyOneDeclarationFound("usage_explicit_ns.k", ".a.b.explicitNs", ".a.b.explicitNs");
  }

  @Test
  public void testOtherExplicitNs() {
    assertExactlyOneDeclarationFound("usage_other_explicit_ns.k", ".x._otherExplicitNs", ".x._otherExplicitNs");
  }

  @Test
  public void testOtherImplicitNs() {
    assertExactlyOneDeclarationFound("usage_other_implicit_ns.k", "otherImplicitNs", ".x.otherImplicitNs");
  }

  @Test
  public void testExplicitRootNs() {
    assertExactlyOneDeclarationFound("usage_explicit_root_ns.k", ".explicitRootNs", ".explicitRootNs");
  }

  @Test
  public void testImplicitRootNs() {
    assertExactlyOneDeclarationFound("usage_implicit_root_ns.k", "implicitRootNs", "implicitRootNs");
  }

  @Test
  public void testOtherNoNs() {
    assertExactlyOneDeclarationFound("usage_other_no_ns.k", "otherNoNs", "otherNoNs");
  }

  @Test
  public void testNotFound() {
    assertNoDeclarationFound("usage_only_ns_found.k");
  }

  @Test
  public void testDefWithImplicitNs() {
    assertExactlyOneDeclarationFound("def_with_implicit_ns.k", "noNs", ".same.file.noNs", "def_with_implicit_ns.k");
  }

  @Test
  public void testLocallyDefinedGlobal_Namespaced() {
    assertExactlyOneDeclarationFound("usage_locally_defined_global_namespaced.q", "inside.fn.relative", ".a.b.inside.fn.relative");
  }

  @Test
  public void testLocallyDefinedGlobal_Absolute() {
    assertExactlyOneDeclarationFound("usage_locally_defined_global_absolute.q", ".inside.fn.absolute", ".inside.fn.absolute");
  }

  @Test
  public void testLocallyDefinedGlobal_Amend() {
    assertExactlyOneDeclarationFound("usage_locally_defined_global_amend.q", "globalAmend", ".c.globalAmend");
  }

  @Test
  public void testFound_Performance() throws Exception {
    final String[] input = TestInputFiles.asFilesPaths();
    if (input.length == 0) {
      return;
    }
    myFixture.configureByFiles(input);
    for (int i = 0; i < 5; i++) {
      long start = System.currentTimeMillis();
      assertStrictlyMoreThanOneDeclarationFound("timings-ref-found.q", ".rts.state",
          ImmutableMap.of("rts.q", 4));
      System.out.println("found in " + (System.currentTimeMillis() - start) + "ms");
    }
  }

  @Test
  public void testNotFound_Performance() throws Exception {
    final String[] input = TestInputFiles.asFilesPaths();
    if (input.length == 0) {
      return;
    }
    myFixture.configureByFiles(input);
    for (int i = 0; i < 5; i++) {
      long start = System.currentTimeMillis();
      assertNoDeclarationFound("timings-ref-not-found.q");
      System.out.println("not found:" + (System.currentTimeMillis() - start) + "ms");
    }
  }

  private void assertExactlyOneDeclarationFound(String fromFile, String expectedId, String expectedFqnId) {
    myFixture.configureByFiles("references.k");
    assertExactlyOneDeclarationFound(fromFile, expectedId, expectedFqnId, "references.k");
  }

  private void assertExactlyOneDeclarationFound(
      String fromFile, String expectedId, String expectedFqnId, String expectedFile) {
    myFixture.configureByFiles(fromFile);
    final PsiReference fromRef = myFixture.getReferenceAtCaretPosition(fromFile);
    assertInstanceOf(fromRef, KReference.class);
    final PsiElement from = fromRef.getElement();
    assertInstanceOf(from, KUserId.class);
    PsiElement to = fromRef.resolve();
    PsiElement toRandom = ((KReferenceBase)(fromRef)).resolveFirstUnordered();
    KUserId fromId = (KUserId)from;
    if (fromId.isDeclaration()) {
      assertNull("Declaration should not resolve to itself: " + fromRef.getElement().getText() + " (" + fromFile + ")", to);
      assertNull("Declaration should not resolve to itself: " + fromRef.getElement().getText() + " (" + fromFile + ")", to);
      to = from;
    } else {
      assertNotNull("Unresolved Reference: " + fromRef.getElement().getText() + " (" + fromFile + ")", to);
      assertNotNull("Unresolved Reference: " + fromRef.getElement().getText() + " (" + fromFile + ")", toRandom);
    }
    assertInstanceOf(to, KUserId.class);
    assertInstanceOf(to.getParent(), KAssignment.class);
    if (expectedFile != null) {
      assertEquals("Resolved to unexpected file: ", expectedFile, to.getContainingFile().getVirtualFile().getName());
    }
    final String toId = to.getText();
    final String toFqnId = KUtil.getFqnOrName((KUserId)to);
    assertEquals("explicit id", expectedId, toId);
    assertEquals("implicit id", expectedFqnId, toFqnId);
  }

  private void assertNoDeclarationFound(String fromFile) {
    myFixture.configureByFiles(fromFile);
    final PsiReference from = myFixture.getReferenceAtCaretPosition(fromFile);
    final PsiElement to = from.resolve();
    assertNull("Shouldn't have been resolved: " + from.getElement().getText() + " (" + fromFile + ")", to);
  }

  private void assertStrictlyMoreThanOneDeclarationFound(
      String fromFile, String expectedFqnId, Map<String,Integer> expectedFilesToNumOfDeclarations) {
    myFixture.configureByFiles(fromFile);
    final PsiReference fromRef = myFixture.getReferenceAtCaretPosition(fromFile);
    assertInstanceOf(fromRef, KReference.class);
    final PsiElement from = fromRef.getElement();
    assertInstanceOf(from, KUserId.class);
    PsiElement to = fromRef.resolve();
    KUserId fromId = (KUserId)from;
    if (fromId.isDeclaration()) {
      assertNotSame("Declaration should not resolve to itself: " + fromRef.getElement().getText() + " (" + fromFile + ")", to, from);
    } else {
      assertNotNull("When there are multiple declarations, resolve() should return 1 of them: " + fromRef.getElement().getText() + " (" + fromFile + ")", to);
    }
    Collection<KUserId> toMulti = ((KReferenceBase)(fromRef)).resolveAllOrdered();
    assertTrue("More than one declaration expected", toMulti.size() > 1);
    final Map<String,Integer> actualFilesToNumOfDeclarations = new HashMap<>();
    for (KUserId found : toMulti) {
      assertNotNull(found);
      assertInstanceOf(found, KUserId.class);
      assertInstanceOf(found.getParent(), KAssignment.class);
      String fileName = found.getContainingFile().getName();
      Integer count = actualFilesToNumOfDeclarations.get(fileName);
      if (count == null) {
        count = 1;
      } else {
        count++;
      }
      actualFilesToNumOfDeclarations.put(fileName, count);
      assertEquals(expectedFqnId, found.getDetails().getFqn());
    }
    assertEquals("Unexpected declarations", expectedFilesToNumOfDeclarations, actualFilesToNumOfDeclarations);
  }

}
