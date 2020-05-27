package com.appian.intellij.k.settings;

import static com.intellij.icons.AllIcons.Nodes.PpJar;
import static com.intellij.ui.Colors.DARK_GREEN;

import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;

public class KAuthDriverDialog extends DialogWrapper {
  private JTextField classText;
  private JBList<String> jarsList;
  private JPanel panel;
  private JTextField nameText;
  private JPanel jarsPanel;
  private JButton testDriverButton;
  private JLabel messageLabel;
  private final Function<String,ValidationInfo> nameValidator;

  protected KAuthDriverDialog(
      JPanel panel, Function<String,ValidationInfo> nameValidator) {
    super(panel, true);
    this.nameValidator = nameValidator;
    initDialog();
  }

  private void initDialog() {
    init();
    jarsList.setModel(new CollectionListModel<>());
    jarsPanel.add(createJarsListDecorator().createPanel(),
        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));

    jarsList.setCellRenderer(new ColoredListCellRenderer<String>() {
      @Override
      protected void customizeCellRenderer(
          @NotNull JList<? extends String> list, String value, int index, boolean selected, boolean hasFocus) {
        setIcon(PpJar);
        append(value);
      }
    });

    testDriverButton.addActionListener(event -> {
      try {
        getAuthDriverSpec().newAuthenticator().apply("user:password@host:port");
        messageLabel.setForeground(DARK_GREEN);
        messageLabel.setText("success");
      }
      catch (RuntimeException e) {
        messageLabel.setForeground(JBColor.RED);
        messageLabel.setText(e.getMessage());
      }
    });
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return nameText;
  }

  private void createUIComponents() {
  }

  public KAuthDriverSpec getAuthDriverSpec() {
    return new KAuthDriverSpec(nameText.getText().trim(), classText.getText().trim(), getJarsModel().toList());
  }

  private CollectionListModel<String> getJarsModel() {
    return (CollectionListModel<String>)jarsList.getModel();
  }

  @NotNull
  private ToolbarDecorator createJarsListDecorator() {
    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jarsList);
    decorator.setAddAction(button -> {
      VirtualFile[] choices = FileChooser.chooseFiles(new FileChooserDescriptor(false, false, true, true, false, true),
          panel, null, null);
      if (choices.length != 0) {
        for (VirtualFile f : choices) {
          getJarsModel().add(f.getCanonicalPath());
        }
      }
    });
    return decorator;
  }


  protected ValidationInfo doValidate() {
    String name = nameText.getText(), className = classText.getText();
    if (name == null || name.trim().isEmpty()) {
      return new ValidationInfo("Name is required");
    }

    if (className == null || className.trim().isEmpty()) {
      return new ValidationInfo("Class name is required");
    }

    return nameValidator.apply(name);
  }


  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return panel;
  }

  public void reset(KAuthDriverSpec spec) {
    nameText.setText(spec.getName());
    classText.setText(spec.getClassName());
    getJarsModel().removeAll();
    getJarsModel().addAll(0, spec.getJars());
  }
}
