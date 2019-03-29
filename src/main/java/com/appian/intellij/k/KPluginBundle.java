package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import com.intellij.AbstractBundle;

public class KPluginBundle extends AbstractBundle {
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static final String BUNDLE = "com.appian.intellij.k.messages.KPluginBundle";
  private static final KPluginBundle INSTANCE = new KPluginBundle();

  private KPluginBundle() {
    super(BUNDLE);
  }
}
