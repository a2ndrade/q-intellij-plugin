package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getEditor;

import java.util.Optional;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

/**
 * An action that pops up a list of configured servers
 * and runs selected Q code on the chosen server
 */
public class KRunSelectionPopupAction extends AnAction {
  public KRunSelectionPopupAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Optional<Editor> editor = getEditor(e);
    if (!editor.isPresent() || editor.get().getSelectionModel().getSelectedText() == null) {
      return;
    }

    ListPopup popup = JBPopupFactory.getInstance()
        .createActionGroupPopup("Evaluate Selection On", new KRunSelectionActionGroup(), e.getDataContext(),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);

    popup.showInBestPositionFor(editor.get());
  }
}
