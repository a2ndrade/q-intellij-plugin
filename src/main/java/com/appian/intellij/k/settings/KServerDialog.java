package com.appian.intellij.k.settings;

import static java.awt.Cursor.WAIT_CURSOR;

import java.awt.Color;
import java.awt.Cursor;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;

import kx.c;

public class KServerDialog extends DialogWrapper {
  private final Function<String,ValidationInfo> nameValidator;
  private JPanel panel;
  private JTextField hostText;
  private JTextField userText;
  private JPasswordField passwordField;
  private JCheckBox useTLSCheckBox;
  private JFormattedTextField portText;
  private JTextField nameText;
  private JButton testConnectionButton;
  private JLabel messageLabel;

  private final static Color DARK_GREEN = new JBColor(new Color(0, 155, 0), Color.green);

  public KServerDialog(Function<String,ValidationInfo> nameValidator) {
    super(null);
    this.nameValidator = nameValidator;
    init();
    setTitle("Q Server");
    testConnectionButton.addActionListener(e -> testConnection());
  }

  private void testConnection() {
    ValidationInfo validationInfo = doValidate();
    if (validationInfo != null) {
      clickDefaultButton();
      return;
    }

    Cursor cursor = panel.getCursor();
    try {
      panel.setCursor(Cursor.getPredefinedCursor(WAIT_CURSOR));
      c connection = getConnectionSpec().createConnection();
      try {
        messageLabel.setForeground(DARK_GREEN);
        messageLabel.setText("Success");
      }
      finally {
        connection.close();
      }
    } catch (c.KException | IOException e) {
      messageLabel.setForeground(JBColor.RED);
      messageLabel.setText(e.getMessage());
    } finally {
      panel.setCursor(cursor);
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return panel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return nameText;
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    String name = nameText.getText(), host = hostText.getText(), port = portText.getText();
    if (name == null || name.trim().isEmpty()) {
      return new ValidationInfo("Name is required");
    }

    ValidationInfo nameVal = nameValidator.apply(name);
    if (nameVal != null) {
      return nameVal;
    }

    if (host == null || host.trim().isEmpty() || port == null || port.trim().isEmpty()) {
      return new ValidationInfo("Host/port required");
    }

    try {
      int portNo = Integer.parseInt(port);
      if (portNo <= 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      return new ValidationInfo("Port must be int > 0");
    }
    return null;
  }

  public KServerSpec getConnectionSpec() {
    return new KServerSpec(nameText.getText(), hostText.getText().trim(), Integer.parseInt(portText.getText()),
        useTLSCheckBox.isSelected(), userText.getText().trim(), new String(passwordField.getPassword()));
  }

  void reset(KServerSpec spec) {
    nameText.setText(spec.getName());
    hostText.setText(spec.getHost());
    portText.setValue(spec.getPort());
    userText.setText(spec.getUser());
    passwordField.setText(spec.getPassword());
    useTLSCheckBox.setSelected(spec.useTLS());
  }

  private void createUIComponents() {
    NumberFormatter format = new NumberFormatter(new DecimalFormat("#####"));
    format.setMinimum(1);
    format.setMaximum(99999);
    portText = new JFormattedTextField(format);
  }
}
