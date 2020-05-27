package com.appian.intellij.k.settings;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.intellij.openapi.ui.ValidationInfo;

public class KServerDialogDescriptor {
  private final Function<String,ValidationInfo> nameValidator;
  private final Supplier<List<KAuthDriverSpec>> authDriverChoices;
  private final Consumer<KAuthDriverSpec> newAuthDriverAction;

  public KServerDialogDescriptor(
      Function<String, ValidationInfo> nameValidator, Supplier<List<KAuthDriverSpec>> authDriverChoicesSupplier, Consumer<KAuthDriverSpec>newAuthDriverAction) {
    this.nameValidator = requireNonNull(nameValidator);
    this.authDriverChoices = requireNonNull(authDriverChoicesSupplier);
    this.newAuthDriverAction = requireNonNull(newAuthDriverAction);
  }

  public Function<String,ValidationInfo> getNameValidator() {
    return nameValidator;
  }

  public Supplier<List<KAuthDriverSpec>> getAuthDriverChoices() {
    return authDriverChoices;
  }

  public Consumer<KAuthDriverSpec> getNewAuthDriverAction() {
    return newAuthDriverAction;
  }
}
