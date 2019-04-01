package com.appian.intellij.k.settings;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.intellij.openapi.util.Comparing;

public class KSettingsPanel {
  private JTextField internalPrefixes;
  private JPanel settingsPanel;
  private JTextField internalSubstrings;

  KSettingsPanel() {
  }

  boolean isModified(KSettings settings) {
    return !Comparing.strEqual(internalPrefixes.getText().trim(), settings.getInternalPrefixes()) ||
        !Comparing.strEqual(internalSubstrings.getText().trim(), settings.getInternalSubstrings());
  }

  void apply(KSettings settings) {
    settings.setInternalPrefixes(internalPrefixes.getText());
    settings.setInternalSubstrings(internalSubstrings.getText());
  }

  void reset(KSettings settings) {
    if (settings != null) {
      internalPrefixes.setText(settings.getInternalPrefixes());
      internalSubstrings.setText(settings.getInternalSubstrings());
    } else {
      internalPrefixes.setText("");
      internalSubstrings.setText("");
    }
  }

  JComponent getComponent() {
    return settingsPanel;
  }
}
