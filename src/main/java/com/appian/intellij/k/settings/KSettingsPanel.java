package com.appian.intellij.k.settings;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.intellij.openapi.ui.Messages;
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
  private JBList<KAuthDriverSpec> authDriversList;
  private JPanel authDriversPanel;

  KSettingsPanel() {
    serversList.setCellRenderer(new ColoredListCellRenderer<KServerSpec>() {
      @Override
      protected void customizeCellRenderer(
          @NotNull JList list, KServerSpec value, int index, boolean selected, boolean hasFocus) {
        setIcon(KIcons.QSERVER);
        append(value.toString());
      }
    });

    authDriversList.setCellRenderer(new ColoredListCellRenderer<KAuthDriverSpec>() {
      @Override
      protected void customizeCellRenderer(
          @NotNull JList list, KAuthDriverSpec value, int index, boolean selected, boolean hasFocus) {
        setIcon(KIcons.QAUTHDRIVER);
        append(value.toString());
      }
    });


    serversPanel.add(createServerListDecorator().createPanel(),
        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));

    authDriversPanel.add(createAuthDriversListDecorator().createPanel(),
        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));

    serversList.setModel(new CollectionListModel<>());
    authDriversList.setModel(new CollectionListModel<>());
  }

  @NotNull
  private ToolbarDecorator createServerListDecorator() {
    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(serversList);
    decorator.setAddAction(button -> {
      KServerDialogDescriptor descriptor = new KServerDialogDescriptor(
          this::validateNewServerName,
          ()->getAuthDriversModel().getItems(),
          newAuthDriver->getAuthDriversModel().add(newAuthDriver));
      KServerDialog dialog = new KServerDialog(serversPanel, descriptor);
      dialog.reset(new KServerSpec());
      if (dialog.showAndGet()) {
        getServersModel().add(dialog.getServerSpec());
      }
    });

    decorator.setEditAction(button -> {
      KServerSpec spec = getServersModel().getElementAt(serversList.getSelectedIndex());
      KServerDialogDescriptor descriptor = new KServerDialogDescriptor(
          n->validateExistingServerName(spec, n),
          ()->getAuthDriversModel().getItems(),
          newAuthDriver->getAuthDriversModel().add(newAuthDriver));
      KServerDialog dialog = new KServerDialog(serversPanel, descriptor);
      dialog.reset(spec);
      if (dialog.showAndGet()) {
        getServersModel().setElementAt(dialog.getServerSpec(), serversList.getSelectedIndex());
      }
    });
    return decorator;
  }

  private ValidationInfo validateNewServerName(String n) {
    return getServersModel().toList().stream().anyMatch(s -> n.equals(s.getName())) ? new ValidationInfo(
        "Server named " + n + " already exists") : null;
  }

  private ValidationInfo validateExistingServerName(KServerSpec spec, String n) {
    return getServersModel().toList().stream().anyMatch(s -> s != spec && n.equals(s.getName())) ? new ValidationInfo(
        "Server named " + n + " already exists.") : null;
  }

  @NotNull
  private ToolbarDecorator createAuthDriversListDecorator() {
    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(authDriversList);
    decorator.setAddAction(button -> {
      KAuthDriverDialog dialog = new KAuthDriverDialog(authDriversPanel, this::validateNewDriverName);
      if (dialog.showAndGet()) {
        getAuthDriversModel().add(dialog.getAuthDriverSpec());
      }
    });

    decorator.setEditAction(button -> {
      KAuthDriverSpec spec = getAuthDriversModel().getElementAt(authDriversList.getSelectedIndex());
      KAuthDriverDialog dialog = new KAuthDriverDialog(authDriversPanel, n->validateExistingDriverName(spec, n));
      dialog.reset(spec);
      if (dialog.showAndGet()) {
        KAuthDriverSpec newSpec = dialog.getAuthDriverSpec();
        getAuthDriversModel().setElementAt(newSpec, authDriversList.getSelectedIndex());

        if (!spec.getName().equals(newSpec.getName())) {
          // update servers to use new driver name where appropriate
          List<KServerSpec> servers = getServersModel().toList();
          for (int i = 0; i < servers.size(); ++i) {
            KServerSpec s = servers.get(i);
            if (spec.getName().equals(s.getAuthDriverName())) {
              KServerSpec copy = s.clone();
              copy.setAuthDriverName(newSpec.getName());
              getServersModel().setElementAt(copy, i);
            }
          }
        }
      }
    });

    decorator.setRemoveAction(button->{
      KAuthDriverSpec toRemove = authDriversList.getSelectedValue();
      TreeSet<String> servers = getServers().stream()
          .filter(s -> toRemove.getName().equals(s.getAuthDriverName()))
          .map(KServerSpec::getName)
          .collect(Collectors.toCollection(TreeSet::new));

      if (!servers.isEmpty()) {
        Messages.showErrorDialog("Server" + (servers.size() > 1 ? "s" : "")  + " " + servers + " "+ (servers.size() > 1 ? "are" : "is") + " using driver " + toRemove.getName() + ", cannot remove", "Q Plugin");
        return;
      }

      getAuthDriversModel().remove(toRemove);
    });
    return decorator;
  }


  private ValidationInfo validateExistingDriverName(KAuthDriverSpec spec, String name) {
    return getAuthDriversModel().toList().stream().anyMatch(s -> s != spec && name.equals(s.getName()))
        ? new ValidationInfo("Driver named " + name + " already exists.")
        : null;
  }

  @Nullable
  private ValidationInfo validateNewDriverName(String n) {
    if (n.contains("<") || n.contains(">")) {
      return new ValidationInfo("Name may not contain < or >");
    }

    if (getAuthDriversModel().toList().stream().anyMatch(s->s.getName().equals(n))) {
      return new ValidationInfo("Driver named " + n + " is already defined");
    }
    return null;
  }

  boolean isModified(KSettings settings) {
    return !Comparing.strEqual(internalPrefixes.getText().trim(), settings.getInternalPrefixes()) ||
        !Comparing.strEqual(internalSubstrings.getText().trim(), settings.getInternalSubstrings()) ||
        !getServers().equals(settings.getServers()) ||
        !getAuthDrivers().equals(settings.getAuthDrivers());
  }

  @NotNull
  private List<KServerSpec> getServers() {
    return getServersModel().getItems();
  }

  private List<KAuthDriverSpec> getAuthDrivers() {
    return getAuthDriversModel().getItems();
  }

  private CollectionListModel<KServerSpec> getServersModel() {
    return (CollectionListModel<KServerSpec>)serversList.getModel();
  }

  private CollectionListModel<KAuthDriverSpec> getAuthDriversModel() {
    return (CollectionListModel<KAuthDriverSpec>)authDriversList.getModel();
  }

  void apply(KSettings settings) {
    settings.setInternalPrefixes(internalPrefixes.getText());
    settings.setInternalSubstrings(internalSubstrings.getText());
    settings.setServers(getServers());
    settings.setAuthDrivers(getAuthDrivers());
  }

  void reset(KSettings settings) {
    getServersModel().removeAll();
    getAuthDriversModel().removeAll();
    if (settings != null) {
      internalPrefixes.setText(settings.getInternalPrefixes());
      internalSubstrings.setText(settings.getInternalSubstrings());
      getServersModel().addAll(0, settings.getServers());
      getAuthDriversModel().addAll(0, settings.getAuthDrivers());
    } else {
      internalPrefixes.setText("");
      internalSubstrings.setText("");
    }
  }

  JComponent getComponent() {
    return settingsPanel;
  }
}
