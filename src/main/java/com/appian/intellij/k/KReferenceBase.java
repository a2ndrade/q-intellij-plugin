package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;

public abstract class KReferenceBase extends PsiReferenceBase<PsiElement> {

  private static final Comparator<PsiElement> START_OFFSET_COMPARATOR = Comparator.comparingInt(
      (PsiElement e) -> e.getTextRange().getStartOffset());
  private static final Comparator<KUserId> RESOLVE_RESULT_COMPARATOR = (a, b) -> {
    // favor non-qualified declarations (which would be shorter)
    int i = Integer.compare(a.getText().length(), b.getText().length());
    if (i == 0) {
      // then, favor declarations under an explicit namespace declaration
      final String aImplicitNs = KUtil.getCurrentNamespace(a);
      final String bImplicitNs = KUtil.getCurrentNamespace(b);
      if (!Objects.equals(aImplicitNs, bImplicitNs)) {
        final String ns = KUtil.getRootContextName(a.getText());
        if (ns != null) {
          if (ns.equals(aImplicitNs)) {
            i = -1;
          } else if (ns.equals(bImplicitNs)) {
            i = 1;
          }
        }
      }
    }
    if (i == 0) {
      // then, favor top-level declarations
      final KAssignment aAssignment = (KAssignment)a.getParent();
      final KAssignment bAssignment = (KAssignment)b.getParent();
      int iA = aAssignment.isTopLevelGlobal() ? 0 : 1;
      int iB = bAssignment.isTopLevelGlobal() ? 0 : 1;
      i = Integer.compare(iA, iB);
    }
    if (i == 0) {
      // then, favor the position of the declaration in their containing file
      i = START_OFFSET_COMPARATOR.compare(a, b);
    }
    if (i == 0) {
      // finally, use their containing file names to break ties
      i = a.getContainingFile().getName().compareTo(b.getContainingFile().getName());
    }
    return i;
  };

  // element is KUserId or KSymbol
  KReferenceBase(PsiElement element, TextRange textRange, boolean soft) {
    super(element, textRange, soft);
  }

  /**
    See
   {@link #resolveFirstUnordered()}
   {@link #resolveAllOrdered()}
   */
  @Override
  public final PsiElement resolve() {
    final Collection<KUserId> declarations = resolveAllOrdered();
    if (declarations.isEmpty()) {
      return null;
    }
    KUserId declaration = declarations.iterator().next();
    // do not resolve a declaration to itself
    return declaration == myElement ? null : declaration;
  }

  @Nullable
  final PsiElement resolveFirstUnordered() {
    for (KUserId declaration : resolve0(true)) {
      if (declaration != null && declaration != myElement) {
        // do not resolve a declaration to itself
        return declaration;
      }
    }
    return null;
  }

  @NotNull
  final Collection<KUserId> findDeclarations(
      Project project, VirtualFile sameFile, boolean stopAfterFirstMatch, KUtil.ExactGlobalAssignmentMatcher matcher) {
    Collection<KUserId> found = findDeclarations0(project, sameFile, stopAfterFirstMatch, matcher);
    if (found.isEmpty()) {
      // last-resort: retry without skipping any files
      return findDeclarations0(project, sameFile, stopAfterFirstMatch, matcher.withSkipCacheCheck());
    }
    return found;
  }

  @NotNull
  private Collection<KUserId> findDeclarations0(
      Project project, VirtualFile sameFile, boolean stopAfterFirstMatch, KUtil.ExactGlobalAssignmentMatcher matcher) {
    final Collection<KUserId> r = new LinkedHashSet<>(stopAfterFirstMatch ? 1 : 16);
    final KUserIdCache cache = KUserIdCache.getInstance();
    // check globals (this file)
    r.addAll(cache.findGlobalDeclarations(project, sameFile, stopAfterFirstMatch, matcher));
    if (stopAfterFirstMatch && !r.isEmpty()) {
      return r;
    }
    // check globals (other files)
    final Collection<VirtualFile> otherFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    for (VirtualFile otherFile : otherFiles) {
      if (sameFile.equals(otherFile)) {
        continue;
      }
      r.addAll(cache.findGlobalDeclarations(project, otherFile, stopAfterFirstMatch, matcher));
      if (stopAfterFirstMatch && !r.isEmpty()) {
        return r;
      }
    }
    return r;
  }

  @Override
  public final boolean isReferenceTo(PsiElement element) {
    if (element == myElement) {
      return false;
    }
    final PsiManager manager = myElement.getManager();
    for (KUserId result : resolveAllOrdered()) {
      if (manager.areElementsEquivalent(result, element)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  final Collection<KUserId> resolveAllOrdered() {
    final Collection<KUserId> kUserIds = resolve0(false);
    final KUserId[] kUserIds1 = kUserIds.toArray(new KUserId[0]);
    Arrays.sort(kUserIds1, RESOLVE_RESULT_COMPARATOR);
    return Arrays.asList(kUserIds1);
  }

  @NotNull
  abstract Collection<KUserId> resolve0(boolean stopAfterFirstMatch);

  @Override
  public final Object[] getVariants() {
    return new Object[0];
  }

  @Override
  public abstract PsiElement handleElementRename(@NotNull String newName);

  static final String getNewNameForUsage(@Nullable KUserId declaration, @NotNull PsiElement usage, @NotNull String newName) {
    final boolean isNewNameAbsolute = KUtil.isAbsoluteId(newName);
    final String declarationImplicitNs = KUtil.getCurrentNamespace(declaration);
    final String usageImplicitNs = KUtil.getCurrentNamespace(usage);
    final String effectiveNewName;
    if (isNewNameAbsolute) {
      effectiveNewName = newName;
    } else if ("".equals(declarationImplicitNs)) {
      effectiveNewName = newName;
    } else if ("".equals(usageImplicitNs)) {
      effectiveNewName = declarationImplicitNs + "." + newName;
    } else {
      if (Objects.equals(declarationImplicitNs, usageImplicitNs)) {
        effectiveNewName = newName;
      } else {
        effectiveNewName = declarationImplicitNs + "." + newName;
      }
    }
    return effectiveNewName;
  }

}
