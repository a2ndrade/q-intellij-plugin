package com.appian.intellij.k.actions;

import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.settings.KServerDialog;
import com.appian.intellij.k.settings.KServerSpec;
import com.appian.intellij.k.settings.KSettings;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;

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
  private final KServerSpec spec;

  KRunSelectionAction(
      Project project, @Nullable String text, @Nullable KServerSpec spec) {
    super(text, null, spec == null ? null : KIcons.QSERVER);
    this.project = project;
    this.spec = spec;
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

    if (spec == null) {
      KSettings settings = KSettingsService.getInstance().getSettings();
      KServerDialog dialog = new KServerDialog(
          n -> settings.getServers().stream().anyMatch(s -> s.getName().equals(n)) ? new ValidationInfo(
              "Server named " + n + "' already exists") : null);

      if (dialog.showAndGet()) {
        ArrayList<KServerSpec> newServers = new ArrayList<>(settings.getServers());
        newServers.add(dialog.getConnectionSpec());
        KSettings newSettings = settings.clone();
        newSettings.setServers(newServers);
        KSettingsService.getInstance().setSettings(newSettings);
        execute(dialog.getConnectionSpec(), selectedText);
      }
    } else {
      execute(spec, selectedText);
    }
  }

  private void execute(KServerSpec spec, String q) {
    RunContentDescriptor contentDescriptor = KRunnerUtil.showRunContent(spec, project);
    ConsoleView console = (ConsoleView)contentDescriptor.getExecutionConsole();
    KServerProcessHandler processHandler = (KServerProcessHandler)contentDescriptor.getProcessHandler();
    if (processHandler == null) { // can't really happen, but...
      console.print("Internal error, please close the console and try again", ERROR_OUTPUT);
    } else {
      processHandler.execute(console, q);
    }
  }
}
