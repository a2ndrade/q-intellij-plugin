package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getSelectedFunctionDefinition;
import static com.appian.intellij.k.actions.KActionUtil.getSelectedServer;

import java.util.Optional;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;

/**
 * An action that defines selected function using currently
 * active server or pops up a menu to choose one.
 */
public class KDefineElementOnActiveServerAction extends AnAction {
  public KDefineElementOnActiveServerAction() {
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Optional<PsiElement> f = getSelectedFunctionDefinition(e);
    if (!f.isPresent()) {
      return;
    }

    Optional<String> activeServerId = getSelectedServer(e.getProject());
    if (activeServerId.isPresent()) {
      new KDefineElementAction(getEventProject(e), null, activeServerId.get()).actionPerformed(e);
    } else {
      new KDefineElementPopupAction().actionPerformed(e);
    }
  }
}
