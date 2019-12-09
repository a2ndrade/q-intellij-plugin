package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KNamedElement;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.impl.StartMarkAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler;
import com.intellij.refactoring.rename.inplace.MemberInplaceRenamer;
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler;

public final class KVariableInplaceRenameHandler extends VariableInplaceRenameHandler {

  /*
   * Had to delegate to MemberInplaceRenameHandler instead of extending it because an `instanceof` check on
   * `RenameHandlerRegistry#getProcessHand#getRenameHandler` that was preventing subclasses handlers from
   * taking effect.
   */
  private final class MemberInplaceRenameHandlerDelegate extends MemberInplaceRenameHandler {
    @Override
    public boolean isAvailable(PsiElement element, Editor editor, PsiFile file) {
      return super.isAvailable(element, editor, file);
    }

    @Override
    public InplaceRefactoring doRename(
        @NotNull PsiElement elementToRename, Editor editor, DataContext dataContext) {
      return super.doRename(elementToRename, editor, dataContext);
    }

    @NotNull
    @Override
    public MemberInplaceRenamer createMemberRenamer(
        @NotNull PsiElement element, PsiNameIdentifierOwner elementToRename, Editor editor) {
      return super.createMemberRenamer(element, elementToRename, editor);
    }
  }

  private final MemberInplaceRenameHandlerDelegate delegate = new MemberInplaceRenameHandlerDelegate();

  @Override
  protected boolean isAvailable(PsiElement element, Editor editor, PsiFile file) {
    if (!(element instanceof KUserId)) {
      return false;
    }
    return delegate.isAvailable(element, editor, file);
  }

  // copied from MemberInplaceRenameHandler
  @Override
  public InplaceRefactoring doRename(@NotNull final PsiElement elementToRename, final Editor editor, final DataContext dataContext) {
    if (elementToRename instanceof PsiNameIdentifierOwner) {
      final RenamePsiElementProcessor processor = RenamePsiElementProcessor.forElement(elementToRename);
      if (processor.isInplaceRenameSupported()) {
        final StartMarkAction startMarkAction = StartMarkAction.canStart(elementToRename.getProject());
        if (startMarkAction == null || processor.substituteElementToRename(elementToRename, editor) == elementToRename) {
          processor.substituteElementToRename(elementToRename, editor, new Pass<PsiElement>() {
            @Override
            public void pass(PsiElement element) {
              final MemberInplaceRenamer renamer = createMemberRenamer(element, (PsiNameIdentifierOwner)elementToRename, editor);
              boolean startedRename = renamer.performInplaceRename();
              if (!startedRename) {
                performDialogRename(elementToRename, editor, dataContext, renamer.getInitialName());
              }
            }
          });
          return null;
        }
        else {
          final InplaceRefactoring inplaceRefactoring = editor.getUserData(InplaceRefactoring.INPLACE_RENAMER);
          if (inplaceRefactoring != null && inplaceRefactoring.getClass() == MemberInplaceRenamer.class) {
            final TemplateState templateState = TemplateManagerImpl.getTemplateState(InjectedLanguageUtil.getTopLevelEditor(editor));
            if (templateState != null) {
              templateState.gotoEnd(true);
            }
          }
        }
      }
    }
    performDialogRename(elementToRename, editor, dataContext, null);
    return null;
  }

  @NotNull
  protected MemberInplaceRenamer createMemberRenamer(@NotNull PsiElement element, PsiNameIdentifierOwner elementToRename, Editor editor) {
    if (elementToRename instanceof KUserId) {
      final KNamedElement.Info details = ((KUserId)elementToRename).getDetails();
      // Given the following snippet. This modification handles proper renaming across namespaces for
      // non-qualified references. e.g.
      //
      //   \d .ns
      //   global:{...}
      //   \d .other
      //   .ns.global[]
      //
      // the problem was that current id being renamed changes (e.g. `.ns.global` to `global`) but because
      // it's in a different namespace (e.g. `\d .other`) the reference resolution code cannot longer find
      // it (because it's really now `.other.global`) so it can never be renamed. It gets always stuck
      // with the original name of the unqualified target e.g. `global`
      final String fqn = details.getFqn();
      return new MemberInplaceRenamer(elementToRename, element, editor, elementToRename.getName(), fqn);
    }
    return delegate.createMemberRenamer(element, elementToRename, editor);
  }

}
