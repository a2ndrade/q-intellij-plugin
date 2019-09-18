package com.appian.intellij.k.actions;

import static com.appian.intellij.k.KUtil.cast;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;

import java.awt.BorderLayout;
import java.util.Optional;

import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.settings.KServerDialog;
import com.appian.intellij.k.settings.KServerSpec;
import com.appian.intellij.k.settings.KSettings;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

class KActionUtil {
  static RunContentDescriptor showRunContent(Project project, String serverId) {
    RunContentManager contentManager = ExecutionManager.getInstance(project).getContentManager();
    RunContentDescriptor runContentDescriptor = findContentDescriptor(project, serverId).orElseGet(
        () -> createAndShowRunContent(project, serverId));

    ToolWindow toolWindow = contentManager.getToolWindowByDescriptor(runContentDescriptor);
    if (toolWindow != null) {
      toolWindow.show(null);
    }
    contentManager.selectRunContent(runContentDescriptor);
    return runContentDescriptor;
  }

  @NotNull
  private static Optional<RunContentDescriptor> findContentDescriptor(Project project, String serverId) {
    return ExecutionManager.getInstance(project)
        .getContentManager()
        .getAllDescriptors()
        .stream()
        .filter(
            e -> e.getProcessHandler() instanceof KServerProcessHandler && ((KServerProcessHandler) e.getProcessHandler()).getServerId().equals(serverId))
        .findFirst();
  }

  @NotNull
  static Optional<String> getSelectedServer(Project project) {
    RunContentDescriptor selectedContent = ExecutionManager.getInstance(project)
        .getContentManager()
        .getSelectedContent();

    if (selectedContent == null || !(selectedContent.getProcessHandler() instanceof KServerProcessHandler)) {
      return Optional.empty();
    } else {
      return Optional.of(((KServerProcessHandler)selectedContent.getProcessHandler()).getServerId());
    }
  }

  @NotNull
  private static RunContentDescriptor createAndShowRunContent(Project project, String serverId) {
    ConsoleViewImpl consoleView = new ConsoleViewImpl(project, true);
    KServerProcessHandler processHandler = new KServerProcessHandler(serverId);
    processHandler.startNotify();
    consoleView.attachToProcess(processHandler);

    DefaultActionGroup toolbarActions = new DefaultActionGroup();

    JPanel panel = new JPanel(new BorderLayout());

    panel.add(consoleView.getComponent(), "Center");
    ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("unknown", toolbarActions, false);
    toolbar.setTargetComponent(consoleView.getComponent());
    panel.add(toolbar.getComponent(), "West");

    RunContentDescriptor runDescriptor = new RunContentDescriptor(consoleView, processHandler, panel, serverId,
        KIcons.QSERVER);

    toolbarActions.addAll(consoleView.createConsoleActions());
    Executor executor = new DefaultRunExecutor();
    toolbarActions.add(new CloseAction(executor, runDescriptor, project));

    ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, runDescriptor);
    return runDescriptor;
  }

  static Optional<KServerSpec> promptForNewServer() {
    KSettings settings = KSettingsService.getInstance().getSettings();
    KServerDialog dialog = new KServerDialog(
        n -> settings.getServers().stream().anyMatch(s -> s.getName().equals(n)) ? new ValidationInfo(
            "Server named " + n + "' already exists") : null);

    return dialog.showAndGet() ? Optional.of(dialog.getConnectionSpec()) : Optional.empty();
  }

  static Optional<PsiElement> getElementAtCaret(AnActionEvent e) {
    Optional<Editor> editor = getEditor(e);
    if (!editor.isPresent()) {
      return Optional.empty();
    }

    PsiFile file = PSI_FILE.getData(e.getDataContext());
    if (file == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(file.findElementAt(editor.get().getCaretModel().getOffset()));
  }

  static Optional<String> getEditorSelection(AnActionEvent e) {
    return getEditor(e).map(editor -> editor.getSelectionModel().getSelectedText());
  }

  @SuppressWarnings("WeakerAccess")
  static Optional<Navigatable> getNavigableSelection(AnActionEvent e) {
    return Optional.ofNullable(CommonDataKeys.NAVIGATABLE.getData(e.getDataContext()));
  }

  @SuppressWarnings("WeakerAccess")
  static Optional<PsiElement> getNavigableSelectionElement(AnActionEvent e) {
    return getNavigableSelection(e).flatMap(n -> cast(n, AbstractTreeNode.class))
        .map(AbstractTreeNode::getValue)
        .flatMap(n -> cast(n, StructureViewTreeElement.class))
        .map(StructureViewTreeElement::getValue)
        .flatMap(o -> cast(o, PsiElement.class));
  }

  @NotNull
  static Optional<Editor> getEditor(AnActionEvent e) {
    return Optional.ofNullable(CommonDataKeys.EDITOR.getData(e.getDataContext()));
  }

  @NotNull
  static Optional<PsiElement> getSelectedFunctionDefinition(AnActionEvent event) {
    //noinspection SimplifyOptionalCallChains
    return Optional.ofNullable(getElementAtCaret(event).flatMap(KUtil::getTopLevelFunctionDefinition)
        .orElseGet(
            () -> getNavigableSelectionElement(event).flatMap(KUtil::getTopLevelFunctionDefinition).orElse(null)));
  }
}
