package com.appian.intellij.k;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KTopLevelAssignment;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;

public final class KUtil {

  public static Collection<KUserId> findIdentifiers(Project project) {
    final Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance()
      .getContainingFiles(FileTypeIndex.NAME, KFileType.INSTANCE, GlobalSearchScope.allScope(project));
    final Stream<KUserId> stream = virtualFiles.stream()
        .flatMap(virtualFile -> findIdentifiers(project, virtualFile));
    List<KUserId> fnNames = stream.collect(Collectors.toList());
    return fnNames;
  }

  public static Stream<KUserId> findIdentifiers(Project project, VirtualFile virtualFile) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(virtualFile);
    if (kFile == null) {
      return Stream.empty();
    }
    final Collection<KTopLevelAssignment> topLevelAssignments = PsiTreeUtil.findChildrenOfType(kFile, KTopLevelAssignment.class);
    if (topLevelAssignments == null) {
      return Stream.empty();
    }
    return topLevelAssignments.stream().map(KTopLevelAssignment::getUserId);
  }
}
