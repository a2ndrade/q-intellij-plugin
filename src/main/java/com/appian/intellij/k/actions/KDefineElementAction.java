package com.appian.intellij.k.actions;

import static com.appian.intellij.k.KUtil.getFqnOrName;
import static com.appian.intellij.k.actions.KActionUtil.getSelectedFunctionDefinition;
import static com.appian.intellij.k.actions.KActionUtil.promptForNewServer;
import static com.appian.intellij.k.actions.KActionUtil.showRunContent;
import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.psi.KUserId;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;

/**
 * An action that redefines selected  in the editor or structure view function
 * on the specified server or prompts for a new server if none was provided.
 * A new console named after chosen server will be open and
 * re-used for subsequent commands sent to the same server.
 * Q code is mirrored in the console, results and errors
 * are output into the console too.
 */
public class KDefineElementAction extends AnAction {
  private final Project project;
  @Nullable
  private final String serverId;

  KDefineElementAction(
      Project project, @Nullable String text, @Nullable String serverId) {
    super(text, null, serverId == null ? null : KIcons.QSERVER);
    this.project = project;
    this.serverId = serverId;
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    Optional<PsiElement> f = getSelectedFunctionDefinition(event);
    if (!f.isPresent()) {
      return;
    }

    if (serverId == null) {
      promptForNewServer().ifPresent(newServer -> {
        KSettingsService.getInstance().updateSettings(settings -> settings.cloneWithNewServer(newServer));
        execute(newServer.getId(), f.get());
      });
    } else {
      execute(serverId, f.get());
    }
  }

  private void execute(String serverId, PsiElement element) {
    RunContentDescriptor contentDescriptor = showRunContent(project, serverId);
    ConsoleView console = (ConsoleView)contentDescriptor.getExecutionConsole();
    KServerProcessHandler processHandler = (KServerProcessHandler)contentDescriptor.getProcessHandler();
    if (processHandler == null) { // can't really happen, but...
      console.print("Internal error, please close the console and try again", ERROR_OUTPUT);
    } else {
      // clone function definition, replace var name with fully qualified name,
      // then use fully qualified version to send to the server
      KUserId userId = (KUserId)element.getFirstChild().getFirstChild();
      PsiElement copy = element.copy();
      ((LeafPsiElement)copy.getFirstChild().getFirstChild().getFirstChild()).replaceWithText(getFqnOrName(userId));

      processHandler.execute(project, console, copy.getText());
    }
  }
}
