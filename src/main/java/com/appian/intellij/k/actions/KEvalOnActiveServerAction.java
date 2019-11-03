package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getEditorEvalText;
import static com.appian.intellij.k.actions.KActionUtil.getSelectedServer;
import static com.appian.intellij.k.actions.KActionUtil.isInQFile;

import java.util.Optional;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * An action that runs selected Q code using currently
 * active server or pops up a menu to choose one.
 */
public class KEvalOnActiveServerAction extends AnAction {
  public KEvalOnActiveServerAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    if (!isInQFile(e.getDataContext())) {
      return;
    }

    if (!getEditorEvalText(e.getDataContext()).isPresent()) {
      return;
    }

    Optional<String> activeServerId = getSelectedServer(e.getProject());
    if (activeServerId.isPresent()) {
      new KEvalAction(getEventProject(e), null, activeServerId.get()).actionPerformed(e);
    } else {
      new KEvalPopupAction().actionPerformed(e);
    }
  }
}
