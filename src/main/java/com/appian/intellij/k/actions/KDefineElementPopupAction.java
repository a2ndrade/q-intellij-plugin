package com.appian.intellij.k.actions;

import static com.appian.intellij.k.KUtil.getFqnOrName;
import static com.appian.intellij.k.actions.KActionUtil.getEditor;
import static com.appian.intellij.k.actions.KActionUtil.getElementAtCaret;

import java.util.Optional;

import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.psi.PsiElement;

/**
 * An action that pops up a list of configured servers
 * and defines selected function on the chosen server
 */
public class KDefineElementPopupAction extends AnAction {
  public KDefineElementPopupAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Optional<Editor> editor = getEditor(e);
    if (!editor.isPresent()) {
      return;
    }

    Optional<PsiElement> f = getElementAtCaret(e).flatMap(KUtil::getTopLevelFunctionDefinition);
    if (!f.isPresent()) {
      return;
    }

    ListPopup popup = JBPopupFactory.getInstance()
        .createActionGroupPopup(
            "Redefine '" + getFqnOrName((KUserId)f.get().getFirstChild().getFirstChild()) + "' On",
            new KDefineElementActionGroup(), e.getDataContext(), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);

    popup.showInBestPositionFor(editor.get());
  }
}
