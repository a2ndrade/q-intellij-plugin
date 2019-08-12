package com.appian.intellij.k.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;

/**
 * An action group consisting of an action to run selected
 * code on the new server or any of the servers, defined
 * in settings.
 */
public class KRunSelectionActionGroup extends ActionGroup {
  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    if (e == null) {
      return new AnAction[0];
    }

    List<KRunSelectionAction> serverActions = ServiceManager.getService(KSettingsService.class)
        .getSettings()
        .getServers()
        .stream()
        .map(s -> new KRunSelectionAction(e.getProject(), s.toString(), s))
        .collect(Collectors.toList());

    List<AnAction> actions = new ArrayList<>();

    if (!serverActions.isEmpty()) {
      actions.addAll(serverActions);
      actions.add(Separator.getInstance());
    }

    actions.add(new KRunSelectionAction(e.getProject(), "New Server...", null));
    return actions.toArray(new AnAction[0]);
  }

  @Override
  public void update(AnActionEvent event) {
    // Enable/disable depending on whether user is editing
    Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (editor == null) {
      return;
    }

    String selectedText = Optional.ofNullable(event.getData(CommonDataKeys.EDITOR))
        .map(e -> e.getSelectionModel().getSelectedText())
        .orElse(null);

    event.getPresentation().setEnabled(selectedText != null);

    // Always make visible.
    event.getPresentation().setVisible(true);
    // Take this opportunity to set an icon for the menu entry.
    event.getPresentation().setIcon(AllIcons.General.Run);
  }
}
