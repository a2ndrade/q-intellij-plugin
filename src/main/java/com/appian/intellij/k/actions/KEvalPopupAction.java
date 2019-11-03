package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getEditor;
import static com.appian.intellij.k.actions.KActionUtil.getEditorEvalText;
import static com.appian.intellij.k.actions.KActionUtil.getEditorSelection;
import static com.appian.intellij.k.actions.KActionUtil.isInQFile;
import static com.appian.intellij.k.actions.KActionUtil.showErrorHint;

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
public class KEvalPopupAction extends AnAction {
  KEvalPopupAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    if (!isInQFile(e.getDataContext())) {
      return;
    }

    Optional<Editor> editor = getEditor(e.getDataContext());
    Optional<String> text = getEditorEvalText(e.getDataContext());
    if (!editor.isPresent() || !text.isPresent()) {
      return;
    }

    if (text.get().trim().isEmpty()) {
      showErrorHint(e, "Nothing to evaluate");
      return;
    }

    ListPopup popup = JBPopupFactory.getInstance()
        .createActionGroupPopup(
            "Evaluate " + (getEditorSelection(e.getDataContext()).isPresent() ? "Selection On" : "Line On"),
            new KEvalActionGroup(), e.getDataContext(), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);

    popup.showInBestPositionFor(editor.get());
  }
}
