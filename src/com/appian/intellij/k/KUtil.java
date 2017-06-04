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

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KExpression;
import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KModeDirective;
import com.appian.intellij.k.psi.KNamespaceDeclaration;
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

public final class KUtil {

  private static final Key<String> FQN = Key.create("fqn");

  public static Collection<KUserId> findProjectIdentifiers(Project project) {
    final Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    final Stream<KUserId> stream = virtualFiles.stream()
        .flatMap(file -> findIdentifiers(project, file, AnyMatcher, false).stream());
    List<KUserId> fnNames = stream.collect(Collectors.toList());
    return fnNames;
  }

  public static Collection<KUserId> findIdentifiers(Project project, VirtualFile file) {
    return findIdentifiers(project, file, AnyMatcher, false);
  }

  @Nullable
  public static KUserId findFirstExactMatch(Project project, VirtualFile file, String targetIdentifier) {
    final Iterator<KUserId> it = findIdentifiers(project, file, new ExactMatcher(targetIdentifier), true).iterator();
    return it.hasNext() ? it.next() : null;
  }

  interface Matcher {
    boolean matches(String found);
  }
  static class PrefixMatcher implements Matcher {
    private final String target;
    public PrefixMatcher(String target) {
      this.target = target;
    }
    @Override
    public boolean matches(String found) {
      return found.startsWith(target);
    }
  }

  static class ExactMatcher implements Matcher {
    private final String target;
    public ExactMatcher(String target) {
      this.target = target;
    }
    @Override
    public boolean matches(String found) {
      return target.equals(found);
    }
  }

  static Matcher AnyMatcher = (found) -> true;

    @NotNull
  static Collection<KUserId> findIdentifiers(
        Project project, VirtualFile file, Matcher matcher, boolean stopAfterFirstMatch) {
    final KFile kFile = (KFile)PsiManager.getInstance(project).findFile(file);
    if (kFile == null) {
      return Collections.emptyList();
    }
    String currentNamespace = ""; // default namespace
    PsiElement topLevelElement = kFile.getFirstChild();
    final Collection<KUserId> results = new ArrayList<>(0);
    do {
      if (topLevelElement instanceof KNamespaceDeclaration) {
        final String newNamespace = ((KNamespaceDeclaration)topLevelElement).getUserId().getText();
        currentNamespace = getNewNamespace(currentNamespace, newNamespace);
      } else {
        final KUserId userId = getTopLevelAssignmentId(topLevelElement);
        if (userId != null) {
          final String userIdName = userId.getName();
          final String userIdNamespace = getExplicitNamespace(userIdName);
          boolean match = false;
          if (userIdNamespace == null && !currentNamespace.isEmpty()) {
            final String fqn = generateFqn(currentNamespace, userIdName);
            if (matcher.matches(fqn)) {
              putFqn(userId, fqn);
              match = true;
            }
          } else if (matcher.matches(userIdName)) {
            match = true;
          }
          if (match) {
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

  @Nullable
  private static KUserId getTopLevelAssignmentId(PsiElement element) {
    if (element instanceof KModeDirective) { // q) or k)
      element = ((KModeDirective)element).getExpression();
    }
    if (element instanceof KExpression && element.getFirstChild() instanceof KAssignment) {
      return ((KAssignment)element.getFirstChild()).getUserId();
    }
    return null;
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
    return findIdentifiers(project, file).stream()
        .map(id -> {
          final String ns = getExplicitNamespace(id.getName());
          return ns == null ? "" : ns;
        })
        .collect(Collectors.toSet());
  }

  @NotNull
  static String getDescriptiveName(@NotNull KUserId element) {
    final String fqnOrName = KUtil.getFqnOrName(element);
    return getFunctionDefinition(element).map((KLambda lambda) -> {
      final KLambdaParams lambdaParams = lambda.getLambdaParams();
      final String paramsText = Optional.ofNullable(lambdaParams)
          .map(KLambdaParams::getUserIdList)
          .map(params -> params.isEmpty() ? "" : lambdaParams.getText())
          .orElse("");
      return fqnOrName + paramsText;
    }).orElse(fqnOrName);
  }

  static Optional<KLambda> getFunctionDefinition(@NotNull KUserId element) {
    final KExpression expression = PsiTreeUtil.getNextSiblingOfType(element, KExpression.class);
    if (expression != null && expression.getFirstChild() instanceof KLambda) {
      return Optional.of((KLambda)expression.getFirstChild());
    }
    return Optional.empty();
  }

  static boolean isInQFile(PsiElement element) {
    return isInFileWithExt(element, "q");
  }

  private static boolean isInFileWithExt(PsiElement element, String extension) {
    return Optional.ofNullable(element.getContainingFile())
        .map(PsiFile::getVirtualFile)
        .map(KUtil::isQFile)
        .isPresent();
  }

  static boolean isQFile(VirtualFile file) {
    return Optional.ofNullable(file)
        .map(VirtualFile::getExtension)
        .filter(ext -> ext.equals("q"))
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

  static String generateFqn(String namespace, String identifier) {
    if (namespace.isEmpty()) {
      return identifier; // default namespace
    } else if (".".equals(namespace)) {
      return "." + identifier; // root namespace
    }
    return namespace + "." + identifier;
  }

  @Nullable
  static String getExplicitNamespace(String identifier) {
    return isAbsoluteId(identifier) ? identifier.substring(0, identifier.lastIndexOf('.')) : null;
  }

  static boolean isAbsoluteId(String identifier) {
    return identifier.charAt(0) == '.';
  }

  @NotNull
  static String getCurrentNamespace(KUserId element) {
    final Class[] potentialContainerTypes = new Class[] {KAssignment.class, KExpression.class,
        KNamespaceDeclaration.class};
    PsiElement topLevelAssignment = null;
    for (Class containerType : potentialContainerTypes) {
      topLevelAssignment = PsiTreeUtil.getTopmostParentOfType(element, containerType);
      if(topLevelAssignment != null) {
        break;
      }
    }
    if (topLevelAssignment == null) {
      return "";
    }
    final KNamespaceDeclaration enclosingNsDeclaration = PsiTreeUtil.getPrevSiblingOfType(
        topLevelAssignment, KNamespaceDeclaration.class);
    if (enclosingNsDeclaration == null) {
      return "";
    }
    String currentNamespace = ""; // default namespace
    final PsiFile containingFile = element.getContainingFile();
    PsiElement topLevelElement = containingFile.getFirstChild();
    do {
      if (topLevelElement instanceof KNamespaceDeclaration) {
        final String ns = ((KNamespaceDeclaration)topLevelElement).getUserId().getText();
        currentNamespace = getNewNamespace(currentNamespace, ns);
      }
      if (topLevelElement == enclosingNsDeclaration) {
        return currentNamespace;
      }
      topLevelElement = topLevelElement.getNextSibling();
    } while (topLevelElement != null);
    throw new IllegalStateException(
        "Cannot calculate current namespace for " + element.getName() + " (" + containingFile.getName() +
            ")");
  }

  private static String getNewNamespace(String currentNamespace, String newNamespace) {
    if (isAbsoluteId(newNamespace)) {
      return newNamespace;
    } else if (currentNamespace.isEmpty()) {
      if ("^".equals(newNamespace)) {
        return ""; // can't go up any further
      }
      // relative ns becomes absolute if we're in the default ns
      return  "." + newNamespace;
    } else if ("^".equals(newNamespace)) { // k3: pop up most-nested newNamespace
      final String newNs = getExplicitNamespace(currentNamespace);
      return (newNs == null || newNs.isEmpty()) ? "" : newNs;
    } else {
      return currentNamespace + '.' + newNamespace;
    }
  }

  public static void putFqn(KUserId element, String fqn) {
    putUserData(element, FQN, fqn);
  }

  @Nullable
  public static String getFqn(KUserId element) {
    return getUserData(element, FQN);
  }

  @NotNull
  public static String getFqnOrName(KUserId element) {
    final String fqn = getFqn(element);
    return fqn != null ? fqn : element.getName();
  }

  private static <T> void putUserData(PsiElement element, Key<T> key, T data) {
    element.putUserData(key, data);
  }

  private static <T> T getUserData(PsiElement element, Key<T> key) {
    return element.getUserData(key);
  }

}
