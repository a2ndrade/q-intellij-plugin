package com.appian.intellij.k.actions;

import static com.appian.intellij.k.KUtil.cast;
import static com.appian.intellij.k.KUtil.first;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static com.intellij.openapi.progress.ProgressIndicatorProvider.getGlobalProgressIndicator;
import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.Optional;

import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.settings.KAuthDriverSpec;
import com.appian.intellij.k.settings.KServerDialog;
import com.appian.intellij.k.settings.KServerDialogDescriptor;
import com.appian.intellij.k.settings.KServerSpec;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
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
        .filter(e -> e.getProcessHandler() instanceof KServerProcessHandler &&
            ((KServerProcessHandler)e.getProcessHandler()).getServerId().equals(serverId))
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

  static Optional<KServerSpec> promptForNewServer(Project project) {
    KServerDialogDescriptor descriptor = new KServerDialogDescriptor(KActionUtil::validateNewServerName,
        () -> KSettingsService.getInstance()
            .getSettings()
            .getAuthDrivers()
            .stream()
            .sorted()
            .collect(toList()),
        newAuthDriver-> KSettingsService.getInstance().updateSettings(settings->settings.cloneWithNewAuthDriver(newAuthDriver)));
    KServerDialog dialog = new KServerDialog(project, descriptor);
    dialog.reset(new KServerSpec());
    return dialog.showAndGet() ? Optional.of(dialog.getServerSpec()) : Optional.empty();
  }

  @Nullable
  private static ValidationInfo validateNewServerName(String n) {
    return KSettingsService.getInstance().getSettings().getServers().stream().anyMatch(s -> s.getName().equals(n))
        ? new ValidationInfo("Server named " + n + "' already exists")
        : null;
  }

  static Optional<PsiElement> getElementAtCaret(DataContext dataContext) {
    Optional<Editor> editor = getEditor(dataContext);
    if (!editor.isPresent()) {
      return Optional.empty();
    }

    PsiFile file = PSI_FILE.getData(dataContext);
    if (file == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(file.findElementAt(editor.get().getCaretModel().getOffset()));
  }

  static Optional<String> getEditorSelection(DataContext dataContext) {
    return getEditor(dataContext).map(editor -> editor.getSelectionModel().getSelectedText());
  }

  @SuppressWarnings("WeakerAccess")
  static Optional<String> getEditorLine(DataContext dataContext) {
    return getEditor(dataContext).map(editor -> {
      Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
      Document doc = editor.getDocument();
      return doc.getText(new TextRange(primaryCaret.getVisualLineStart(), primaryCaret.getVisualLineEnd()));
    });
  }

  static Optional<String> getEditorEvalText(DataContext dataContext) {
    return first(()-> getEditorSelection(dataContext), ()->getEditorLine(dataContext));
  }

  @SuppressWarnings("WeakerAccess")
  static Optional<Navigatable> getNavigableSelection(DataContext dataContext) {
    return Optional.ofNullable(CommonDataKeys.NAVIGATABLE.getData(dataContext));
  }

  @SuppressWarnings("WeakerAccess")
  static Optional<PsiElement> getNavigableSelectionElement(DataContext dataContext) {
    return getNavigableSelection(dataContext).flatMap(n -> cast(n, AbstractTreeNode.class))
        .map(AbstractTreeNode::getValue)
        .flatMap(n -> cast(n, StructureViewTreeElement.class))
        .map(StructureViewTreeElement::getValue)
        .flatMap(o -> cast(o, PsiElement.class));
  }

  @NotNull
  static Optional<Editor> getEditor(DataContext dataContext) {
    return Optional.ofNullable(CommonDataKeys.EDITOR.getData(dataContext));
  }

  @NotNull
  static Optional<VirtualFile> getVirtualFile(DataContext dataContext) {
    return Optional.ofNullable(CommonDataKeys.VIRTUAL_FILE.getData(dataContext));
  }

  @NotNull
  static Optional<PsiElement> getSelectedFunctionDefinition(DataContext dataContext) {
    return Optional.ofNullable(getElementAtCaret(dataContext).flatMap(KUtil::getTopLevelFunctionDefinition)
        .orElseGet(
            () -> getNavigableSelectionElement(dataContext).flatMap(KUtil::getTopLevelFunctionDefinition).orElse(null)));
  }

  static void showInformationNotification(Project project, String message) {
    String displayId = "Q Plugin Notifications";
    NotificationGroup group = Optional.ofNullable(NotificationGroup.findRegisteredGroup(displayId))
        .orElseGet(() -> NotificationGroup.toolWindowGroup(displayId, ToolWindowId.RUN));

    group.createNotification(message, NotificationType.INFORMATION).notify(project);
  }

  static void showErrorHint(AnActionEvent event, String text) {
    Optional<Editor> editor = getEditor(event.getDataContext());
    if (!editor.isPresent()) {
      return;
    }
    if (event.getInputEvent() instanceof KeyEvent) {
      HintManager.getInstance().showErrorHint(editor.get(), text);
    }
    else {
      // when popup menu is used to trigger an action,
      // editor hint does not work for some reason, use
      // popup instead
      JBPopupFactory.getInstance().createMessage(text).showInBestPositionFor(editor.get());
    }
  }

  static void runInBackground(Runnable runnable) {
    ApplicationManager.getApplication()
        .executeOnPooledThread(() -> ProgressManager.getInstance()
            .runInReadActionWithWriteActionPriority(runnable, getGlobalProgressIndicator()));
  }

  static boolean isInQFile(DataContext dataContext) {
    return getVirtualFile(dataContext).map(f -> "q".equals(f.getExtension())).orElse(false);
  }
}
