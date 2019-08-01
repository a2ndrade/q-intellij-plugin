package com.appian.intellij.k.actions;

import java.util.Optional;

import com.appian.intellij.k.settings.KServerSpec;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;

/**
 * An action that runs selected Q code using currently
 * active server or pops up a menu to choose one.
 */
public class KRunSelectionOnActiveServerAction extends AnAction {
  public KRunSelectionOnActiveServerAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
    if (editor == null) {
      return;
    }
    String selectedText = editor.getSelectionModel().getSelectedText();
    if (selectedText == null) {
      return;
    }

    Optional<KServerSpec> activeServer = KRunnerUtil.getSelectedServer(e.getProject());

    if (activeServer.isPresent()) {
      new KRunSelectionAction(getEventProject(e), null, activeServer.get()).actionPerformed(e);
      return;
    }

    new KRunSelectionPopupAction().actionPerformed(e);
  }
}
