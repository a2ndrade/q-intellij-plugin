package com.appian.intellij.k.settings;

import static com.appian.intellij.k.settings.KAuthDriverSpec.getBasicAuthenticator;
import static java.awt.Cursor.WAIT_CURSOR;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.JBColor;

import kx.c;

public class KServerDialog extends DialogWrapper {
  public static final String DEFAULT_AUTH_METHOD = "<Default>";
  public static final String NEW_AUTH_METHOD = "<New...>";

  private JPanel panel;
  private JTextField hostText;
  private JTextField userText;
  private JPasswordField passwordField;
  private JCheckBox useTLSCheckBox;
  private JFormattedTextField portText;
  private JTextField nameText;
  private JButton testConnectionButton;
  private JLabel messageLabel;
  private JComboBox<String> authMethodCombo;

  private final static Color DARK_GREEN = new JBColor(new Color(0, 155, 0), JBColor.GREEN);
  private final KServerDialogDescriptor descriptor;

  public KServerDialog(@Nullable Project project, KServerDialogDescriptor descriptor) {
    super(project);
    this.descriptor = requireNonNull(descriptor);
    initDialog();
  }

  public KServerDialog(JPanel serversPanel, KServerDialogDescriptor descriptor) {
    super(serversPanel, false);
    this.descriptor = requireNonNull(descriptor);
    initDialog();
  }

  private volatile String previousAuthMethod;
  private void initDialog() {
    init();
    setTitle("Q Server");
    authMethodCombo.setModel(new CollectionComboBoxModel<>());
    testConnectionButton.addActionListener(e -> testConnection());
    authMethodCombo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        previousAuthMethod = (String) e.getItem();
      }
    });

    authMethodCombo.addActionListener(a->{
      Object selectedItem = authMethodCombo.getSelectedItem();
      if (NEW_AUTH_METHOD.equals(selectedItem)) {
        KAuthDriverDialog dialog = new KAuthDriverDialog(panel, this::validateNewDriverName);
        if (dialog.showAndGet()) {
          descriptor.getNewAuthDriverAction().accept(dialog.getAuthDriverSpec());
          updateAuthMethodChoices();
          authMethodCombo.setSelectedItem(dialog.getAuthDriverSpec().getName());
        }
        else {
          authMethodCombo.setSelectedItem(previousAuthMethod);
        }
      }
    });
  }

  @Nullable
  private ValidationInfo validateNewDriverName(String n) {
    if (n.contains("<") || n.contains(">"))
      return new ValidationInfo("Name may not contain < or >");

    if (getAuthDriversModel().toList().contains(n))
      return new ValidationInfo("Driver named " + n + " is already defined");
    return null;
  }

  private CollectionComboBoxModel<String> getAuthDriversModel() {
    return (CollectionComboBoxModel<String>)authMethodCombo.getModel();
  }

  private void testConnection() {
    ValidationInfo validationInfo = doValidate();
    if (validationInfo != null) {
      clickDefaultButton();
      return;
    }
    ApplicationManager.getApplication()
        .executeOnPooledThread(() -> ProgressManager.getInstance()
            .runInReadActionWithWriteActionPriority(this::doTestConnection,
                ProgressIndicatorProvider.getGlobalProgressIndicator()));

  }

  private void doTestConnection() {
    Cursor cursor = panel.getCursor();
    try {
      panel.setCursor(Cursor.getPredefinedCursor(WAIT_CURSOR));
      messageLabel.setForeground(JBColor.PINK);
      messageLabel.setText("connecting...");
      KServerSpec spec = getServerSpec();
      c connection = spec.createConnection(getAuthenticator(spec.getAuthDriverName()));
      try {
        messageLabel.setForeground(DARK_GREEN);
        messageLabel.setText("success");
      } finally {
        connection.close();
      }
    } catch (c.KException | IOException | RuntimeException e) {
      messageLabel.setForeground(JBColor.RED);
      messageLabel.setText(e.getMessage());
    } finally {
      panel.setCursor(cursor);
    }
  }

  private Function<String,String> getAuthenticator(String authDriverName) {
    if (authDriverName == null) {
      return getBasicAuthenticator();
    }

    KAuthDriverSpec spec = descriptor.getAuthDriverChoices()
        .get()
        .stream()
        .filter(e -> authDriverName.equals(e.getName()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Authentication driver named " + authDriverName + " is not defined"));

    return spec.newAuthenticator();
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

    ValidationInfo nameVal = descriptor.getNameValidator().apply(name);
    if (nameVal != null) {
      return nameVal;
    }

    if (host == null || host.trim().isEmpty() || port == null || port.trim().isEmpty()) {
      return new ValidationInfo("Host/port required");
    }

    try {
      int portNo = parseInt(port);
      if (portNo <= 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      return new ValidationInfo("Port must be int > 0");
    }
    return null;
  }

  public KServerSpec getServerSpec() {
    KServerSpec spec = new KServerSpec();
    apply(spec);
    return spec;
  }

  void apply(KServerSpec spec) {
    spec.setName(nameText.getText());
    spec.setHost(hostText.getText().trim());
    spec.setPort(parseInt(portText.getText()));
    spec.setUseTLS(useTLSCheckBox.isSelected());
    spec.setUser(userText.getText().trim());
    spec.setPassword(new String(passwordField.getPassword()));
    spec.setAuthDriverName(DEFAULT_AUTH_METHOD.equals(authMethodCombo.getSelectedItem()) ? null : (String) authMethodCombo.getSelectedItem());
  }

  public void reset(KServerSpec spec) {
    nameText.setText(spec.getName());
    hostText.setText(spec.getHost());
    portText.setValue(spec.getPort());
    userText.setText(spec.getUser());
    passwordField.setText(spec.getPassword());
    useTLSCheckBox.setSelected(spec.useTLS());
    updateAuthMethodChoices();
    authMethodCombo.setSelectedItem(spec.getAuthDriverName() == null ? DEFAULT_AUTH_METHOD : spec.getAuthDriverName());
  }

  private void updateAuthMethodChoices() {
    getAuthDriversModel().removeAll();
    getAuthMethodChoices().forEach(s->getAuthDriversModel().add(s));
  }

  private  List<String> getAuthMethodChoices() {
    return concat(concat(Stream.of(DEFAULT_AUTH_METHOD),
        descriptor.getAuthDriverChoices().get().stream().map(KAuthDriverSpec::getName)),
        Stream.of(NEW_AUTH_METHOD)).collect(toList());
  }

  private void createUIComponents() {
    NumberFormatter format = new NumberFormatter(new DecimalFormat("#####"));
    format.setMinimum(1);
    format.setMaximum(99999);
    portText = new JFormattedTextField(format);
  }
}
