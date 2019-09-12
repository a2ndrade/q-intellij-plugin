package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.getEditorSelection;
import static com.appian.intellij.k.actions.KActionUtil.promptForNewServer;
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
 * An action that runs selected code in the editor on the specified
 * server or  prompts for a new server if none was provided.
 * A new console named after chosen server will be open and
 * re-used for subsequent commands sent to the same server.
 * Q code is mirrored in the console, results and errors
 * are output into the console too.
 */
public class KRunSelectionAction extends AnAction {
  private final Project project;
  @Nullable
  private final String serverId;

  KRunSelectionAction(
      Project project, @Nullable String text, @Nullable String serverId) {
    super(text, null, serverId == null ? null : KIcons.QSERVER);
    this.project = project;
    this.serverId = serverId;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    getEditorSelection(e).ifPresent(selection -> {
      if (serverId == null) {
        promptForNewServer().ifPresent(newServer -> {
          KSettingsService.getInstance().updateSettings(settings -> settings.cloneWithNewServer(newServer));
          execute(newServer.getId(), selection);
        });
      } else {
        execute(serverId, selection);
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
      processHandler.execute(console, q);
    }
  }
}
