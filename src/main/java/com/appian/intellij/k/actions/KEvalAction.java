package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getEditorEvalText;
import static com.appian.intellij.k.actions.KActionUtil.isInQFile;
import static com.appian.intellij.k.actions.KActionUtil.promptForNewServer;
import static com.appian.intellij.k.actions.KActionUtil.showErrorHint;
import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * An action that runs selection or current line in the editor on the specified
 * server or prompts for a new server if none was provided.
 * A new console named after chosen server will be open and
 * re-used for subsequent commands sent to the same server.
 * Q code is mirrored in the console, results and errors
 * are output into the console too.
 */
public class KEvalAction extends AnAction {
  private final Project project;
  @Nullable
  private final String serverId;

  KEvalAction(
      Project project, @Nullable String text, @Nullable String serverId) {
    super(text, null, serverId == null ? null : KIcons.QSERVER);
    this.project = project;
    this.serverId = serverId;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    if (!isInQFile(e.getDataContext())) {
      return;
    }

    getEditorEvalText(e.getDataContext()).ifPresent(t -> {
      if (t.trim().isEmpty()) {
        showErrorHint(e, "Nothing to evaluate");
        return;
      }

      if (serverId == null) {
        promptForNewServer(e.getProject()).ifPresent(newServer -> {
          KSettingsService.getInstance().updateSettings(settings -> settings.cloneWithNewServer(newServer));
          execute(newServer.getId(), t);
        });
      } else {
        execute(serverId, t);
      }
    });
  }

  private void execute(String serverId, String q) {
    RunContentDescriptor contentDescriptor = KActionUtil.showRunContent(project, serverId);
    ConsoleView console = (ConsoleView)contentDescriptor.getExecutionConsole();
    KServerProcessHandler processHandler = (KServerProcessHandler)contentDescriptor.getProcessHandler();
    if (processHandler == null) { // can't really happen, but...
      console.print("Internal error, please close the console and try again", ERROR_OUTPUT);
    } else {
      processHandler.execute(project, console, q);
    }
  }
}
