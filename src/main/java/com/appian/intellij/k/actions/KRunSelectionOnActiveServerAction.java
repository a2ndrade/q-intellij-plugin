package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getEditorSelection;
import static com.appian.intellij.k.actions.KActionUtil.getSelectedServer;

import java.util.Optional;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * An action that runs selected Q code using currently
 * active server or pops up a menu to choose one.
 */
public class KRunSelectionOnActiveServerAction extends AnAction {
  public KRunSelectionOnActiveServerAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    if (!getEditorSelection(e).isPresent()) {
      return;
    }

    Optional<String> activeServerId = getSelectedServer(e.getProject());
    if (activeServerId.isPresent()) {
      new KRunSelectionAction(getEventProject(e), null, activeServerId.get()).actionPerformed(e);
    } else {
      new KRunSelectionPopupAction().actionPerformed(e);
    }
  }
}
