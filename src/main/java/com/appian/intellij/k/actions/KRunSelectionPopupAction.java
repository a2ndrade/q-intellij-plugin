package com.appian.intellij.k.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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
    Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
    if (editor == null) {
      return;
    }

    String selectedText = editor.getSelectionModel().getSelectedText();
    if (selectedText == null) {
      return;
    }

    ListPopup popup = JBPopupFactory.getInstance()
        .createActionGroupPopup("Run Selection On", new KRunSelectionActionGroup(), e.getDataContext(),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);

    popup.showInBestPositionFor(editor);
  }
}
