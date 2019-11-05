package com.appian.intellij.k.psi;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface KNamedElement extends PsiNameIdentifierOwner {

  String[] IMPLICIT_VARS = new String[] {"x", "y", "z"};

  int UNKNOWN_ACCESS_LEVEL = -1;
  int PRIVATE_ACCESS_LEVEL = 0;
  int PUBLIC_ACCESS_LEVEL = 1;

  class Info {
    private final boolean globalAssignment;
    private final Supplier<String> lazyFqn;
    private final BooleanSupplier lazyGlobal;
    public Info(String fqn, boolean global, boolean globalAssignment) {
      this(()->fqn, global, globalAssignment);
    }
    public Info(Supplier<String> lazyFqn, boolean global, boolean globalAssignment) {
      this(lazyFqn, ()->global, globalAssignment);
    }
    public Info(Supplier<String> lazyFqn, BooleanSupplier lazyGlobal, boolean globalAssignment) {
      this.globalAssignment = globalAssignment;
      this.lazyFqn = lazyFqn;
      this.lazyGlobal = lazyGlobal;
    }
    /**
     * `true` if this is a global assignment or a reference to a global variable
     */
    public boolean isGlobal() {
      return lazyGlobal.getAsBoolean();
    }
    /**
     * `true` if this is a global assignment, even if it's locally performed
     */
    public boolean isGlobalAssignment() {
      return globalAssignment;
    }
    @NotNull
    public String getFqn() {
      return lazyFqn.get();
    }
  }

  boolean isDeclaration();

  @NotNull
  Info getDetails();

  boolean isColumnDeclaration();

  boolean isInternal();

  ItemPresentation getPresentation();

  int getAccessLevel();

  @NotNull
  String getName();
}
