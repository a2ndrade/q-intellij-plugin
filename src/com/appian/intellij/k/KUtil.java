package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KNamespaceDefinition;
import com.appian.intellij.k.psi.KTopLevelAssignment;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;

public final class KUtil {

  public static Collection<KUserId> findProjectIdentifiers(Project project) {
    final Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    final Stream<KUserId> stream = virtualFiles.stream()
        .flatMap(virtualFile -> findFileIdentifiers(project, virtualFile).stream());
    List<KUserId> fnNames = stream.collect(Collectors.toList());
    return fnNames;
  }

  public static Collection<KUserId> findFileIdentifiers(Project project, VirtualFile virtualFile) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(virtualFile);
    if (kFile == null) {
      return Collections.emptyList();
    }
    final Collection<KUserId> topLevelIdentifiers = new ArrayList<>();
    PsiElement topLevelElement = kFile.getFirstChild();
    do {
      if (topLevelElement instanceof KTopLevelAssignment) {
        final KUserId userId = ((KTopLevelAssignment)topLevelElement).getUserId();
        topLevelIdentifiers.add(userId);
      }
      topLevelElement = topLevelElement.getNextSibling();
    } while (topLevelElement != null);
    return topLevelIdentifiers;
  }

  @Nullable
  public static KUserId findMatchingIdentifier(Project project, VirtualFile virtualFile, String targetName) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(virtualFile);
    if (kFile == null) {
      return null;
    }
    String currentNamespace = null;
    PsiElement topLevelElement = kFile.getFirstChild();
    do {
      if (topLevelElement instanceof KNamespaceDefinition) {
        currentNamespace = ((KNamespaceDefinition)topLevelElement).getUserId().getText();
      } else if (topLevelElement instanceof KTopLevelAssignment) {
        final KUserId userId = ((KTopLevelAssignment)topLevelElement).getUserId();
        final String userIdName = userId.getName();
        final String userIdNamespace = KUserIdCache.getExplicitNamespace(userIdName);
        if (targetName.equals(userIdName) || (userIdNamespace == null && currentNamespace != null &&
            targetName.equals(KUserIdCache.generateFqn(currentNamespace, userIdName)))) {
          return userId;
        }
      }
      topLevelElement = topLevelElement.getNextSibling();
    } while (topLevelElement != null);
    return null;
  }

  public static Map<String, Set<VirtualFile>> findProjectNamespaces(Project project) {
    final Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    final Map<String,Set<VirtualFile>> namespaceToVirtualFile = new HashMap<>();
    virtualFiles.forEach(virtualFile -> {
      final Set<String> namespaces = findFileNamespaces(project, virtualFile);
      for (String namespace : namespaces) {
        final Set<VirtualFile> filesWhereNamespace = namespaceToVirtualFile.computeIfAbsent(namespace,
            k -> new LinkedHashSet<>());
        filesWhereNamespace.add(virtualFile);
      }
    });
    return namespaceToVirtualFile;
  }

  public static Set<String> findFileNamespaces(Project project, VirtualFile virtualFile) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(virtualFile);
    if (kFile == null) {
      return Collections.emptySet();
    }
    final Set<String> namespaces = new LinkedHashSet<>();
    PsiElement topLevelElement = kFile.getFirstChild();
    do {
      if (topLevelElement instanceof KNamespaceDefinition) {
        final String currentNamespace = ((KNamespaceDefinition)topLevelElement).getUserId().getText();
        namespaces.add(currentNamespace);
      } else if (topLevelElement instanceof KTopLevelAssignment) {
        final KUserId userId = ((KTopLevelAssignment)topLevelElement).getUserId();
        final String topLevelAssignmentNamespace = KUserIdCache.getExplicitNamespace(userId.getName());
        if (topLevelAssignmentNamespace != null) {
          namespaces.add(topLevelAssignmentNamespace);
        }
      }
      topLevelElement = topLevelElement.getNextSibling();
    } while (topLevelElement != null);
    return namespaces;
  }

}
