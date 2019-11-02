package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
import com.appian.intellij.k.psi.KNamedElement;
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
    return stream.collect(Collectors.toList());
  }

  public static Collection<KUserId> findIdentifiers(Project project, VirtualFile file) {
    return findIdentifiers(project, file, AnyMatcher, false);
  }

  @Nullable
  public static KUserId findFirstExactMatch(Project project, VirtualFile file, String targetIdentifier) {
    final Iterator<KUserId> it = findIdentifiers(project, file, new ExactMatcher(targetIdentifier), true).iterator();
    return it.hasNext() ? it.next() : null;
  }

  @SafeVarargs
  public static <T> Optional<T> first(Supplier<Optional<T>>... suppliers) {
    return Arrays.stream(suppliers)
        .map(Supplier::get)
        .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
        .findFirst();
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

  private static final Matcher AnyMatcher = (found) -> true;

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
    if (topLevelElement == null) {
      return results;
    }
    do {
      if (topLevelElement instanceof KNamespaceDeclaration) {
        final String newNamespace = ((KNamespaceDeclaration)topLevelElement).getUserId().getText();
        currentNamespace = getNewNamespace(currentNamespace, newNamespace);
      } else {
        // there are multiple ids if they are chained i.e. x:y:z:1
        for (KUserId userId : getTopLevelAssignmentIds(topLevelElement)) {
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

  @NotNull
  private static List<KUserId> getTopLevelAssignmentIds(PsiElement topLevelElement) {
    if (topLevelElement instanceof KModeDirective) { // q) or k)
      topLevelElement = ((KModeDirective)topLevelElement).getExpression();
    }
    final List<KUserId> ids = new ArrayList<>();
    PsiElement currentElement = topLevelElement;
    while (currentElement instanceof KExpression && currentElement.getFirstChild() instanceof KAssignment) {
      final KAssignment assignment = (KAssignment)currentElement.getFirstChild();
      ids.add(assignment.getUserId());
      currentElement = assignment.getExpression();
    }
    return ids;
  }

  public static Map<String,Set<VirtualFile>> findProjectNamespaces(Project project) {
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
    return findIdentifiers(project, file).stream().map(id -> {
      final String ns = getExplicitNamespace(id.getName());
      return ns == null ? "" : ns;
    }).collect(Collectors.toSet());
  }

  @NotNull
  public static String getDescriptiveName(@NotNull KUserId element) {
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

  public static Optional<KLambda> getFunctionDefinition(@NotNull KNamedElement element) {
    final KExpression expression = PsiTreeUtil.getNextSiblingOfType(element, KExpression.class);
    if (expression != null && expression.getFirstChild() instanceof KLambda) {
      return Optional.of((KLambda)expression.getFirstChild());
    }
    return Optional.empty();
  }

  private static boolean isInQFile(PsiElement element) {
    return isInFileWithExt(element, "q");
  }

  private static boolean isInFileWithExt(PsiElement element, String extension) {
    final PsiFile file = element.getContainingFile();
    return file == null || isFileWithExt(file.getVirtualFile(), extension);
  }

  static boolean isFileWithExt(VirtualFile file, String extension) {
    return file == null || extension.equals(file.getExtension());
  }

  public static boolean isValidIdentifier(PsiElement element) {
    if (!(element instanceof KUserId)) {
      return false;
    }
    // in k3, the Q built-in functions are valid variable names. In Q, they are not
    if (KUtil.isInQFile(element)) {
      PsiElement nameIdentifier = ((KUserId)element).getNameIdentifier();
      return nameIdentifier != null && KTypes.Q_SYSTEM_FUNCTION != nameIdentifier.getNode().getElementType();
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
  public static String getExplicitNamespace(String identifier) {
    return isAbsoluteId(identifier) ? identifier.substring(0, identifier.lastIndexOf('.')) : null;
  }

  static boolean isAbsoluteId(String identifier) {
    return identifier.charAt(0) == '.';
  }

  @NotNull
  static String getCurrentNamespace(PsiElement element) {
    final Class[] potentialContainerTypes = new Class[] {KExpression.class, KNamespaceDeclaration.class};
    PsiElement topLevelAssignment = null;
    for (Class containerType : potentialContainerTypes) {
      topLevelAssignment = PsiTreeUtil.getTopmostParentOfType(element, containerType);
      if (topLevelAssignment != null) {
        break;
      }
    }
    if (topLevelAssignment == null) {
      return "";
    }
    final KNamespaceDeclaration enclosingNsDeclaration = PsiTreeUtil.getPrevSiblingOfType(topLevelAssignment,
        KNamespaceDeclaration.class);
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
        "Cannot calculate current namespace for " + element.getText() + " (" + containingFile.getName() + ")");
  }

  private static String getNewNamespace(String currentNamespace, String newNamespace) {
    if (isAbsoluteId(newNamespace)) {
      return newNamespace;
    } else if (currentNamespace.isEmpty()) {
      if ("^".equals(newNamespace)) {
        return ""; // can't go up any further
      }
      // relative ns becomes absolute if we're in the default ns
      return "." + newNamespace;
    } else if ("^".equals(newNamespace)) { // k3: pop up most-nested newNamespace
      final String newNs = getExplicitNamespace(currentNamespace);
      return (newNs == null || newNs.isEmpty()) ? "" : newNs;
    } else {
      return currentNamespace + '.' + newNamespace;
    }
  }

  public static void putFqn(KNamedElement element, String fqn) {
    element.putUserData(FQN, fqn);
  }

  @Nullable
  public static String getFqn(KNamedElement element) {
    return element.getUserData(FQN);
  }

  @NotNull
  public static String getFqnOrName(KNamedElement element) {
    final String fqn = getFqn(element);
    return fqn != null ? fqn : element.getName();
  }

  private static Optional<PsiElement> findTopLevelMatching(PsiElement element, Predicate<PsiElement> predicate) {
    for (PsiElement e = element; e != null; e = e.getParent()) {
      if (e.getParent() instanceof KFile) {
        return predicate.test(e) ? Optional.of(e) : Optional.empty();
      }
    }
    return Optional.empty();
  }

  public static Optional<PsiElement> getTopLevelFunctionDefinition(PsiElement elem) {
    return findTopLevelMatching(elem, el -> cast(el, KExpression.class).map(PsiElement::getFirstChild)
        .flatMap(e -> cast(e, KAssignment.class))
        .map(PsiElement::getLastChild)
        .flatMap(e -> cast(e, KExpression.class))
        .map(PsiElement::getFirstChild)
        .flatMap(e -> cast(e, KLambda.class))
        .isPresent());
  }

  public static <T> Optional<T> cast(Object o, Class<T> clazz) {
    if (clazz.isInstance(o)) {
      return Optional.of(clazz.cast(o));
    } else {
      return Optional.empty();
    }
  }
}
