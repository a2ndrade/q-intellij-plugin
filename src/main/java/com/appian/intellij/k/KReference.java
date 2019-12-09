package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KNamedElement;
import com.appian.intellij.k.psi.KNamespaceDeclaration;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

public final class KReference extends KReferenceBase {

  KReference(KUserId element, TextRange textRange) {
    super(element, textRange, false);
  }

  @NotNull
  @Override
  Collection<KUserId> resolve0(boolean stopAfterFirstMatch) {
    final KUserId source = (KUserId)this.myElement;
    return resolve00(() -> source.getDetails().getFqn(), stopAfterFirstMatch);
  }

  @NotNull
  private Collection<KUserId> resolve00(Supplier<String> lazyRefName, boolean stopAfterFirstMatch) {
    final VirtualFile sameFile = myElement.getContainingFile().getOriginalFile().getVirtualFile();
    if (sameFile == null || sameFile.getCanonicalPath() == null) {
      return Collections.emptyList();
    }
    final PsiElement context = myElement.getContext();
    if (context instanceof KNamespaceDeclaration) {
      return Collections.emptyList();
    }
    final Project project = myElement.getProject();
    final KUserId reference = (KUserId)myElement;
    if (reference.isColumnDeclaration()) {
      return Collections.emptyList();
    }
    final KUserId foundLocally = findLocalDeclaration(reference);
    if (foundLocally != null) {
      return Collections.singletonList(foundLocally);
    }
    final String referenceName = lazyRefName.get();
    final String fqn = isBuiltinQFunction(sameFile, referenceName) ? (".q." + referenceName) : referenceName;
    KUtil.ExactGlobalAssignmentMatcher matcher = new KUtil.ExactGlobalAssignmentMatcher(fqn);
    return findDeclarations(project, sameFile, stopAfterFirstMatch, matcher);
  }

  @Nullable
  public static KUserId findLocalDeclaration(KNamedElement target) {
    // exit early if an namespaced id is found b/c local assignments are never namespaced/qualified
    final String text = target.getText();
    if (KUtil.isNamespacedId(text)) {
      return null;
    }
    final KLambda enclosingLambda = PsiTreeUtil.getContextOfType(target, KLambda.class);
    // 1) check the enclosing enclosingLambda params
    return Optional.ofNullable(enclosingLambda)
        .map(KLambda::getLambdaParams)
        .map(KLambdaParams::getUserIdList)
        .orElse(Collections.emptyList())
        .stream()
        .filter(id -> text.equals(id.getText()))
        .findFirst()
        .orElse(findLocalAssignment(text, enclosingLambda));
  }

  @Nullable
  private static KUserId findLocalAssignment(String text, KLambda enclosingLambda) {
    List<KAssignment> assignmentsInLambdaScope = PsiTreeUtil.findChildrenOfType(enclosingLambda, KAssignment.class)
        .stream()
        .filter(id -> text.equals(id.getUserId().getText()))
        .collect(Collectors.toList());
    for (KAssignment assignment : assignmentsInLambdaScope) {
      if (assignment.isSyntacticallyGlobalAmend() || assignment.isComposite()) {
        continue;
      }
      return assignment.getUserId();
    }
    return null;
  }

  private static boolean isBuiltinQFunction(VirtualFile file, String referenceName) {
    return Arrays.binarySearch(KCompletionContributor.SYSTEM_FNS_Q, referenceName) > 0 &&
        KUtil.isFileWithExt(file, "q");
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newName) throws IncorrectOperationException {
    final KUserId declaration = (KUserId)resolve();
    final String newEffectiveName = getNewNameForUsage(declaration, myElement, newName);
    ((KUserId)myElement).setName(newEffectiveName);
    KUserIdCache.getInstance().remove(myElement);
    return myElement;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return ((KUserId)myElement).getDetails().getFqn();
  }
}
