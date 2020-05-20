package com.appian.intellij.k.psi.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.KAstWrapperPsiElement;
import com.appian.intellij.k.KIcons;
import com.appian.intellij.k.KReference;
import com.appian.intellij.k.KUserIdCache;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KElementFactory;
import com.appian.intellij.k.psi.KGroupOrList;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KNamedElement;
import com.appian.intellij.k.psi.KNamespaceDeclaration;
import com.appian.intellij.k.psi.KQSql;
import com.appian.intellij.k.psi.KUserId;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public abstract class KNamedElementImpl extends KAstWrapperPsiElement implements KNamedElement {
  KNamedElementImpl(ASTNode node) {
    super(node);
  }

  @NotNull
  public String getName() {
    return getText();
  }

  public PsiElement setName(@NotNull String newName) {
    Optional.ofNullable(KElementFactory.createKUserId(getProject(), newName))
        .map(KUserId::getFirstChild)
        .map(PsiElement::getNode)
        .ifPresent(newKeyNode -> {
          final ASTNode keyNode = getNode().getFirstChildNode();
          getNode().replaceChild(keyNode, newKeyNode);
          KUserIdCache.getInstance().remove(this); // clear file cache to reflect changes immediately
        });
    return this;
  }

  public PsiElement getNameIdentifier() {
    return this;
  }

  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @NotNull
      @Override
      public String getPresentableText() {
        return getDetails().getFqn();
      }

      @NotNull
      @Override
      public String getLocationString() {
        final PsiFile containingFile = getContainingFile();
        return containingFile == null ? "" : containingFile.getName();
      }

      @NotNull
      @Override
      public Icon getIcon(boolean unused) {
        if (isDeclaration()) {
          if (KUtil.getFunctionDefinition(KNamedElementImpl.this).isPresent()) {
            return isInternal() ? KIcons.PRIVATE_FUNCTION : KIcons.PUBLIC_FUNCTION;
          } else {
            return isInternal() ? KIcons.PRIVATE_VARIABLE : KIcons.PUBLIC_VARIABLE;
          }
        }
        return KIcons.FILE;
      }
    };
  }

  public boolean isDeclaration() {
    final PsiElement parent = getParent();
    if (parent instanceof KLambdaParams || parent instanceof KNamespaceDeclaration) {
      return true;
    }
    if (parent instanceof KAssignment) {
      return ((KAssignment)parent).getArgs() == null;
    }
    return false;
  }

  @NotNull
  public Info getDetails() {
    final String text = getName();
    PsiElement parent = getParent();
    if (parent instanceof KLambdaParams || parent instanceof KNamespaceDeclaration) {
      return new Info(text, false, false);
    }
    if (parent instanceof KAssignment) {
      final KAssignment assign = (KAssignment)parent;
      if (assign.isGlobal()) {
        boolean isComposite = assign.isComposite();
        if (KUtil.isAbsoluteId(text)) {
          return new Info(text, true, !isComposite);
        }
        return new Info(() -> {
          final String currentNs = KUtil.getCurrentNamespace(this);
          return KUtil.generateFqn(currentNs, text);
        }, true, !isComposite);
      }
      // local declaration
      return new Info(text, false, false);
    }
    if (KUtil.isAbsoluteId(text)) {
      return new Info(text, true, false);
    }
    final BooleanSupplier isGlobal = () -> {
      // ensure that `a` inside the function is resolved to `.ns.a` AND `x` is resolved to `x`
      //   \d .ns
      //   a:1
      //   fn:{a+x}
      for (String implicitVar : KNamedElement.IMPLICIT_VARS) {
        if (implicitVar.equals(text)) {
          return false;
        }
      }
      // make sure it's not a reference to a local (which is never namespaced)
      return KReference.findLocalDeclaration(this) == null;
    };
    return new Info(() -> {
      if (!isGlobal.getAsBoolean()){
        return text;
      }
      // if not, then use current namespace to infer full-qualified name
      final String currentNs = KUtil.getCurrentNamespace(this);
      return KUtil.generateFqn(currentNs, text);
    }, isGlobal, false);
  }

  public boolean isColumnDeclaration() {
    return isQSqlColumnDeclaration() || isLiteralColumnDeclaration();
  }

  private Boolean isLiteralColumnDeclaration() {
    return Optional.of(getParent())
    .filter(KAssignment.class::isInstance)
    .map(PsiElement::getParent)
    .map(PsiElement::getParent)
    .filter(KGroupOrList.class::isInstance)
    .map(KGroupOrList.class::cast)
    .map(group -> {
      // it needs at list two items to be a table e.g. ([] a:...)
      if (group.getExpressionList().isEmpty()) {
        return false;
      }
      return !group.getExpressionList().get(0).getArgsList().isEmpty();
    })
    .orElse(false);
  }

  private Boolean isQSqlColumnDeclaration() {
    return Optional.of(getParent())
        .filter(KAssignment.class::isInstance)
        .map(PsiElement::getParent)
        .map(PsiElement::getParent)
        .map(KQSql.class::isInstance)
        .orElse(false);
  }

  @Override
  public boolean isEquivalentTo(PsiElement object) {
    if (object instanceof KNamedElement) {
      final Info thisInfo = getDetails();
      if (thisInfo.isGlobal()) {
        final KUserId other = (KUserId)object;
        final Info otherInfo = other.getDetails();
        if (otherInfo.isGlobal()) {
          return Objects.equals(thisInfo.getFqn(), otherInfo.getFqn());
        }
      }
    }
    return false;
  }

  public boolean isInternal() {
    return KSettingsService.getInstance().getSettings().isInternalName(getName());
  }

  @Override
  public int getAccessLevel() {
    return isInternal() ? KNamedElement.PRIVATE_ACCESS_LEVEL : KNamedElement.PUBLIC_ACCESS_LEVEL;
  }

  // Indicates the scope of files in which to find usages of this PsiElement
  // For find usages, this should include all files in the top level project
  @NotNull
  @Override
  public SearchScope getUseScope() {
    return GlobalSearchScope.projectScope(getProject());
  }
}
