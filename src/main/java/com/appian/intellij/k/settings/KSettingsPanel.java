package com.appian.intellij.k.settings;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.KIcons;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;

public class KSettingsPanel {
  private JTextField internalPrefixes;
  private JPanel settingsPanel;
  private JTextField internalSubstrings;
  private JBList<KServerSpec> serversList;
  private JPanel serversPanel;

  KSettingsPanel() {
    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(serversList);

    serversList.setCellRenderer(new ColoredListCellRenderer<KServerSpec>() {
      @Override
      protected void customizeCellRenderer(
          @NotNull JList list, KServerSpec value, int index, boolean selected, boolean hasFocus) {
        setIcon(KIcons.QSERVER);
        append(value.toString());
      }
    });

    decorator.setAddAction(anActionButton -> {
      KServerDialog dialog = new KServerDialog(
          n -> getServersModel().toList().stream().anyMatch(s -> n.equals(s.getName())) ? new ValidationInfo(
              "Server named " + n + " already exists") : null);

      if (dialog.showAndGet()) {
        getServersModel().add(dialog.getConnectionSpec());
      }
    });

    decorator.setEditAction(anActionButton -> {
      KServerSpec spec = getServersModel().getElementAt(serversList.getSelectedIndex());
      KServerDialog dialog = new KServerDialog(
          n -> getServersModel().toList().stream().anyMatch(s -> s != spec && n.equals(s.getName()))
              ? new ValidationInfo("Server named " + n + " already exists.")
              : null);
      dialog.reset(spec);
      if (dialog.showAndGet()) {
        getServersModel().setElementAt(dialog.getConnectionSpec(), serversList.getSelectedIndex());
      }
    });

    serversPanel.add(decorator.createPanel(),
        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));

    serversList.setModel(new CollectionListModel<>(new ArrayList<>()));
  }

  boolean isModified(KSettings settings) {
    return !Comparing.strEqual(internalPrefixes.getText().trim(), settings.getInternalPrefixes()) ||
        !Comparing.strEqual(internalSubstrings.getText().trim(), settings.getInternalSubstrings()) ||
        !getServers().equals(settings.getServers());
  }

  @NotNull
  private List<KServerSpec> getServers() {
    return getServersModel().getItems();
  }

  private CollectionListModel<KServerSpec> getServersModel() {
    return (CollectionListModel<KServerSpec>)serversList.getModel();
  }

  void apply(KSettings settings) {
    settings.setInternalPrefixes(internalPrefixes.getText());
    settings.setInternalSubstrings(internalSubstrings.getText());
    settings.setServers(getServers());
  }

  void reset(KSettings settings) {
    getServersModel().removeAll();
    if (settings != null) {
      internalPrefixes.setText(settings.getInternalPrefixes());
      internalSubstrings.setText(settings.getInternalSubstrings());
      getServersModel().addAll(0, settings.getServers());
    } else {
      internalPrefixes.setText("");
      internalSubstrings.setText("");
    }
  }

  JComponent getComponent() {
    return settingsPanel;
  }
}
