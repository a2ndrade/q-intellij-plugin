package com.appian.intellij.k.settings;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.intellij.openapi.util.Comparing;

public class KSettingsPanel {
  private JTextField privateItemsPrefixes;
  private JPanel settingsPanel;

  KSettingsPanel() {
  }

  boolean isModified(KSettings settings) {
    return !Comparing.strEqual(privateItemsPrefixes.getText().trim(), settings.getInternalPrefixes());
  }

  void apply(KSettings settings) {
    settings.setInternalPrefixes(privateItemsPrefixes.getText());
  }

  void reset(KSettings settings) {
    if (settings != null) {
      privateItemsPrefixes.setText(settings.getInternalPrefixes());
    } else {
      privateItemsPrefixes.setText("");
    }
  }

  JComponent getComponent() {
    return settingsPanel;
  }
}
