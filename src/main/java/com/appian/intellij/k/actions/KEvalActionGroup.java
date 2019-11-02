package com.appian.intellij.k.actions;

import static com.appian.intellij.k.KIcons.RUN_SELECTION;
import static com.appian.intellij.k.actions.KActionUtil.getEditorSelection;
import static com.appian.intellij.k.actions.KActionUtil.isInQFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.components.ServiceManager;

/**
 * An action group consisting of an action to evaluate
 * code on a new server or any of the servers defined
 * in settings.
 */
public class KEvalActionGroup extends ActionGroup {
  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    if (e == null) {
      return new AnAction[0];
    }

    List<KEvalAction> serverActions = ServiceManager.getService(KSettingsService.class)
        .getSettings()
        .getServers()
        .stream()
        .map(s -> new KEvalAction(e.getProject(), s.toString(), s.getId()))
        .collect(Collectors.toList());

    List<AnAction> actions = new ArrayList<>();

    if (!serverActions.isEmpty()) {
      actions.addAll(serverActions);
      actions.add(Separator.getInstance());
    }

    actions.add(new KEvalAction(e.getProject(), "New Server...", null));
    return actions.toArray(new AnAction[0]);
  }

  @Override
  public void update(AnActionEvent e) {
    Presentation p = e.getPresentation();
    p.setVisible(isInQFile(e.getDataContext()));
    p.setIcon(RUN_SELECTION);
    p.setText(getEditorSelection(e.getDataContext()).isPresent() ? "Evaluate Selection On" : "Evaluate Line On");
  }

}
