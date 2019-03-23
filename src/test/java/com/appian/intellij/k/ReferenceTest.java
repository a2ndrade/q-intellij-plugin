package com.appian.intellij.k;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class ReferenceTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/" + getClass().getName().replace('.', '/');
  }

  public void testNoNs() {
    assertReferenceFound("usage_no_ns.k", "noNs", null);
  }

  public void testUsageUnderNs() {
    assertReferenceFound("usage_under_ns.k", "noNs", null);
  }

  public void testImplicitNs() {
    assertReferenceFound("usage_implicit_ns.k", "implicitNs", ".a.b.implicitNs");
  }

  public void testExplicitNs() {
    assertReferenceFound("usage_explicit_ns.k", ".a.b.explicitNs", null);
  }

  public void testOtherExplicitNs() {
    assertReferenceFound("usage_other_explicit_ns.k", ".x._otherExplicitNs", null);
  }

  public void testPopLastNs() {
    assertReferenceFound("usage_pop_last_ns.k", "popLastLevelNs", ".a.popLastLevelNs");
  }

  public void testImplicitSubNs() {
    assertReferenceFound("usage_implicit_sub_ns.k", "implicitSubNs", ".a.c.implicitSubNs");
  }

  public void testOtherImplicitNs() {
    assertReferenceFound("usage_other_implicit_ns.k", "otherImplicitNs", ".x.otherImplicitNs");
  }

  public void testExplicitRootNs() {
    assertReferenceFound("usage_explicit_root_ns.k", ".explicitRootNs", null);
  }

  public void testImplicitRootNs() {
    assertReferenceFound("usage_implicit_root_ns.k", "implicitRootNs", ".implicitRootNs");
  }

  public void testOtherNoNs() {
    assertReferenceFound("usage_other_no_ns.k", "otherNoNs", null);
  }

  public void testNotFound() {
    assertReferenceNotFound("usage_only_ns_found.k");
  }

  public void testDefWithImplicitNs() {
    assertReferenceFound("def_with_implicit_ns.k", "noNs", ".same.file.noNs", "def_with_implicit_ns.k");
  }

  private void assertReferenceFound(String fromFile, String expectedId, String expectedFqnId) {
    assertReferenceFound(fromFile, expectedId, expectedFqnId, "references.k");
  }

  private void assertReferenceFound(
      String fromFile, String expectedId, String expectedFqnId, String expectedFile) {
    myFixture.configureByFiles(fromFile, "references.k");
    final PsiReference fromRef = myFixture.getReferenceAtCaretPosition(fromFile);
    final PsiElement from = fromRef.getElement();
    PsiElement to = fromRef.resolve();
    assertInstanceOf(from, KUserId.class);
    KUserId fromId = (KUserId)from;
    if (fromId.isDeclaration()) {
      assertNull("Declaration should not resolve to itself: " + fromRef.getElement().getText() + " (" + fromFile + ")",
          to);
      to = from;
    } else {
      assertNotNull("Unresolved Reference: " + fromRef.getElement().getText() + " (" + fromFile + ")", to);
    }
    assertInstanceOf(to, KUserId.class);
    assertInstanceOf(to.getContext(), KAssignment.class);
    assertEquals("Resolved to unexpected file: ", expectedFile, to.getContainingFile().getVirtualFile().getName());
    final String toId = to.getText();
    final String toFqnId = KUtil.getFqn((KUserId)to);
    assertEquals("explicit id", expectedId, toId);
    assertEquals("implicit id", expectedFqnId, toFqnId);
  }

  private void assertReferenceNotFound(
      String fromFile) {
    myFixture.configureByFiles(fromFile, "references.k");
    final PsiReference from = myFixture.getReferenceAtCaretPosition(fromFile);
    final PsiElement to = from.resolve();
    assertNull("Shouldn't have been resolved: " + from.getElement().getText() + " (" + fromFile + ")", to);
  }

}
