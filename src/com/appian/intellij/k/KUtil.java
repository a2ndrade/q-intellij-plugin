package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KExpression;
import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KNamespaceDefinition;
import com.appian.intellij.k.psi.KTopLevelAssignment;
import com.appian.intellij.k.psi.KTypes;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

final class KUtil {

  static final Key<String> FQN = Key.create("fqn");

  public static Collection<KUserId> findProjectIdentifiers(Project project) {
    final Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    final Stream<KUserId> stream = virtualFiles.stream()
        .flatMap(file -> findIdentifiers(project, file).stream());
    List<KUserId> fnNames = stream.collect(Collectors.toList());
    return fnNames;
  }

  public static Collection<KUserId> findIdentifiers(Project project, VirtualFile file) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(file);
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
  public static KUserId findFirstExactMatch(Project project, VirtualFile file, String targetIdentifier) {
    final Iterator<KUserId> it = findIdentifiers(project, file, targetIdentifier, true,
        true).iterator();
    return it.hasNext() ? it.next() : null;
  }

  @NotNull
  public static Collection<KUserId> findAllIdentifiers(
      Project project,
      VirtualFile file,
      String targetIdentifier,
      boolean exactMatch) {
    return findIdentifiers(project, file, targetIdentifier, false, exactMatch);
  }


  @NotNull
  static Collection<KUserId> findIdentifiers(
      Project project,
      VirtualFile file,
      String targetIdentifier,
      boolean stopAfterFirstMatch,
      boolean exactMatch) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(file);
    if (kFile == null) {
      return Collections.emptyList();
    }
    String currentNamespace = null;
    PsiElement topLevelElement = kFile.getFirstChild();
    final Collection<KUserId> results = new ArrayList<>(0);
    do {
      if (topLevelElement instanceof KNamespaceDefinition) {
        currentNamespace = ((KNamespaceDefinition)topLevelElement).getUserId().getText();
      } else if (topLevelElement instanceof KTopLevelAssignment) {
        final KUserId userId = ((KTopLevelAssignment)topLevelElement).getUserId();
        final String userIdName = userId.getName();
        final String userIdNamespace = getExplicitNamespace(userIdName);
        if (exactMatch ? targetIdentifier.equals(userIdName) : userIdName.startsWith(targetIdentifier)) {
          results.add(userId);
          if (stopAfterFirstMatch) {
            return results;
          }
        } else if (userIdNamespace == null && currentNamespace != null) {
          final String fqn = generateFqn(currentNamespace, userIdName);
          if (exactMatch ? targetIdentifier.equals(fqn) : fqn.startsWith(targetIdentifier)) {
            userId.putUserData(FQN, fqn);
            results.add(userId);
            if (stopAfterFirstMatch) {
              return results;
            }
          }
        }
      }
      topLevelElement = topLevelElement.getNextSibling();
    } while (topLevelElement != null);
    return results;
  }

  public static Map<String, Set<VirtualFile>> findProjectNamespaces(Project project) {
    final Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    final Map<String,Set<VirtualFile>> namespaceToVirtualFile = new HashMap<>();
    virtualFiles.forEach(file -> {
      final Set<String> namespaces = findFileNamespaces(project, file);
      for (String namespace : namespaces) {
        final Set<VirtualFile> filesWhereNamespace = namespaceToVirtualFile.computeIfAbsent(namespace,
            k -> new LinkedHashSet<>());
        filesWhereNamespace.add(file);
      }
    });
    return namespaceToVirtualFile;
  }

  public static Set<String> findFileNamespaces(Project project, VirtualFile file) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(file);
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
        final String topLevelAssignmentNamespace = getExplicitNamespace(userId.getName());
        if (topLevelAssignmentNamespace != null) {
          namespaces.add(topLevelAssignmentNamespace);
        }
      }
      topLevelElement = topLevelElement.getNextSibling();
    } while (topLevelElement != null);
    return namespaces;
  }

  @NotNull
  static String getDescriptiveName(@NotNull KUserId element) {
    return getFunctionDefinition(element).map((KLambda lambda) -> {
      final KLambdaParams lambdaParams = lambda.getLambdaParams();
      final String paramsText = Optional.ofNullable(lambdaParams)
          .map(KLambdaParams::getUserIdList)
          .map(params -> params.isEmpty() ? "" : lambdaParams.getText())
          .orElse("");
      return element.getName() + paramsText;
    }).orElse(element.getName());
  }

  static Optional<KLambda> getFunctionDefinition(@NotNull KUserId element) {
    final KExpression expression = PsiTreeUtil.getNextSiblingOfType(element, KExpression.class);
    if (expression != null && expression.getFirstChild() instanceof KLambda) {
      return Optional.of((KLambda)expression.getFirstChild());
    }
    return Optional.empty();
  }

  static boolean isInKFile(PsiElement element) {
    return isInFileWithExt(element, "k");
  }

  static boolean isInQFile(PsiElement element) {
    return isInFileWithExt(element, "q");
  }

  private static boolean isInFileWithExt(PsiElement element, String extension) {
    return Optional.ofNullable(element.getContainingFile())
        .map(PsiFile::getVirtualFile)
        .map(VirtualFile::getExtension)
        .filter(ext -> ext.equals(extension))
        .isPresent();
  }

  public static boolean isValidIdentifier(PsiElement element) {
    if (!(element instanceof KUserId)) {
      return false;
    }
    // in k3, the Q built-in functions are valid variable names. In Q, they are not
    if (KUtil.isInQFile(element)) {
      final IElementType elementType = ((KUserId)element).getNameIdentifier().getNode().getElementType();
      return KTypes.Q_SYSTEM_FUNCTION != elementType;
    }
    return true;
  }

  static String generateFqn(String namespace, String fnName) {
    return namespace + "." + fnName;
  }

  @Nullable
  static String getExplicitNamespace(String fnName) {
    return fnName.charAt(0) == '.' ? fnName.substring(0, fnName.lastIndexOf('.')) : null;
  }
}
