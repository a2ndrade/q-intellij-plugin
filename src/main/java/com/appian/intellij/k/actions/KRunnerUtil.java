package com.appian.intellij.k.actions;

import java.awt.BorderLayout;
import java.util.Optional;

import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.settings.KServerSpec;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

class KRunnerUtil {
  static RunContentDescriptor showRunContent(KServerSpec spec, Project project) {
    RunContentManager contentManager = ExecutionManager.getInstance(project).getContentManager();
    RunContentDescriptor runContentDescriptor = findContentDescriptor(project, spec).orElseGet(
        () -> createAndShowRunContent(spec, project));

    ToolWindow toolWindow = contentManager.getToolWindowByDescriptor(runContentDescriptor);
    if (toolWindow != null) {
      toolWindow.show(null);
    }
    contentManager.selectRunContent(runContentDescriptor);
    return runContentDescriptor;
  }

  @NotNull
  private static Optional<RunContentDescriptor> findContentDescriptor(Project project, KServerSpec spec) {
    return ExecutionManager.getInstance(project)
        .getContentManager()
        .getAllDescriptors()
        .stream()
        .filter(
            e -> e.getDisplayName().equals(spec.toString()) && e.getProcessHandler() instanceof KServerProcessHandler)
        .findFirst();
  }

  @NotNull
  static Optional<KServerSpec> getSelectedServer(Project project) {
    RunContentDescriptor selectedContent = ExecutionManager.getInstance(project)
        .getContentManager()
        .getSelectedContent();

    if (selectedContent == null || !(selectedContent.getProcessHandler() instanceof KServerProcessHandler)) {
      return Optional.empty();
    } else {
      return Optional.of(((KServerProcessHandler)selectedContent.getProcessHandler()).getServerSpec());
    }
  }

  @NotNull
  private static RunContentDescriptor createAndShowRunContent(KServerSpec spec, Project project) {
    ConsoleViewImpl consoleView = new ConsoleViewImpl(project, true);
    KServerProcessHandler processHandler = new KServerProcessHandler(spec);
    processHandler.startNotify();
    consoleView.attachToProcess(processHandler);

    DefaultActionGroup toolbarActions = new DefaultActionGroup();

    JPanel panel = new JPanel(new BorderLayout());

    panel.add(consoleView.getComponent(), "Center");
    ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("unknown", toolbarActions, false);
    toolbar.setTargetComponent(consoleView.getComponent());
    panel.add(toolbar.getComponent(), "West");

    RunContentDescriptor runDescriptor = new RunContentDescriptor(consoleView, processHandler, panel, spec.toString(),
        KIcons.FILE);

    toolbarActions.addAll(consoleView.createConsoleActions());
    Executor executor = new DefaultRunExecutor();
    toolbarActions.add(new CloseAction(executor, runDescriptor, project));

    ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, runDescriptor);
    return runDescriptor;
  }
}
