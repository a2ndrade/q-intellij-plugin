package com.appian.intellij.k.settings;

import javax.swing.JComponent;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;

public class KSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {
  private KSettingsPanel panel;

  public KSettingsConfigurable() {
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Q";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    panel = new KSettingsPanel();
    return panel.getComponent();
  }

  @Override
  public boolean isModified() {
    return panel.isModified(KSettingsService.getInstance().getSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    KSettings newSettings = new KSettings();
    panel.apply(newSettings);
    KSettingsService.getInstance().setSettings(newSettings);
  }

  @Override
  public void reset() {
    panel.reset(KSettingsService.getInstance().getSettings());
  }

  @Override
  public void disposeUIResources() {
    panel = null;
  }

  @NotNull
  @Override
  public String getId() {
    return "preferences.Q";
  }
}
