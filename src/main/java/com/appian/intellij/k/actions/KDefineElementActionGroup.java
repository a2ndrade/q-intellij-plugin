package com.appian.intellij.k.actions;

import static com.appian.intellij.k.KIcons.DEFINE_SELECTION;
import static com.appian.intellij.k.KUtil.getFqnOrName;
import static com.appian.intellij.k.actions.KActionUtil.getSelectedFunctionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KUserId;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;

/**
 * An action group consisting of an action to define selected
 * function  on the new server or any of the servers, defined
 * in settings.
 */
public class KDefineElementActionGroup extends ActionGroup {
  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    if (e == null) {
      return new AnAction[0];
    }

    List<KDefineElementAction> serverActions = ServiceManager.getService(KSettingsService.class)
        .getSettings()
        .getServers()
        .stream()
        .map(s -> new KDefineElementAction(e.getProject(), s.toString(), s.getId()))
        .collect(Collectors.toList());

    List<AnAction> actions = new ArrayList<>();

    if (!serverActions.isEmpty()) {
      actions.addAll(serverActions);
      actions.add(Separator.getInstance());
    }

    actions.add(new KDefineElementAction(e.getProject(), "New Server...", null));
    return actions.toArray(new AnAction[0]);
  }

  @Override
  public void update(AnActionEvent event) {
    Optional<PsiElement> f = getSelectedFunctionDefinition(event.getDataContext());

    event.getPresentation().setVisible(f.isPresent());
    event.getPresentation().setEnabled(f.isPresent());

    event.getPresentation().setIcon(DEFINE_SELECTION);

    f.ifPresent(el -> event.getPresentation()
        .setText("Redefine '" + getFqnOrName((KUserId)el.getFirstChild().getFirstChild()) + "' On"));
  }
}
