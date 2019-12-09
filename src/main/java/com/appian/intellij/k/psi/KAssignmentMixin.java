package com.appian.intellij.k.psi;

import com.intellij.psi.PsiElement;

public interface KAssignmentMixin extends PsiElement {

  default boolean isLocal() {
    return !isGlobal();
  }

  /**
   * NOTE: It may be composite and/or namespaced
   */
  default boolean isGlobal() {
    return isNamespaced() || isTopLevelGlobal() || isEffectiveGlobalAmend();
  }

  /**
   * False if a top-level conditional block, for example, wraps the global
   *
   * NOTE: It may be composite and/or namespaced
   */
  boolean isSyntacticallyTopLevelGlobal();

  /**
   * NOTE: It may be composite and/or namespaced
   */
  boolean isTopLevelGlobal();

  /**
   * e.g. a.b.c:1
   *
   * NOTE: It may be composite
   */
  boolean isNamespaced();

  /**
   * e.g. abc::1
   *
   * NOTE: It may be composite, namespaced and/or top-level.
   *
   * This is guaranteed to reference a global.
   **/
  boolean isEffectiveGlobalAmend();

  /**
   * e.g. abc::1
   *
   * NOTE: It may be composite, namespaced and/or top-level.
   *
   * WARN: Even if syntactically a global amend, a non-namespaced global amend may NOT BE REFER TO A GLOBAL
   *       VARIABLE if it appears inside a lambda and there is a previous local declaration (with the same
   *       name) in scope
   */
  boolean isSyntacticallyGlobalAmend();

  /**
   * e.g. abc:1
   *
   * NOTE: It may be composite, namespaced and/or top-level
   */
  default boolean isSyntacticallySimpleAmend() {
    return !isSyntacticallyGlobalAmend();
  }

  /**
   * e.g. a[1]:123
   *
   * NOTE: It may be local or global assignment
   */
  boolean isComposite();

}
