package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KUserId;
import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.java.AccessLevelProvider;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

final class KStructureViewElement
    implements StructureViewTreeElement, SortableTreeElement, AccessLevelProvider {

  private PsiElement element;

  @Override
  public Object getValue() {
    return element;
  }

  public KStructureViewElement(PsiElement element) {
    this.element = element;
  }

  @NotNull
  @Override
  public String getAlphaSortKey() {
    return Strings.nullToEmpty(
        element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null);
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    if (element instanceof KFile) {
      return ((NavigationItem)element).getPresentation();
    }
    return getTreeItemPresentation((KUserId)element);
  }

  @NotNull
  @Override
  public TreeElement[] getChildren() {
    if (!(element instanceof  KFile)) {
      return EMPTY_ARRAY;
    }
    final Project project = element.getProject();
    final VirtualFile virtualFile = ((KFile)element).getVirtualFile();
    final Collection<KUserId> topLevelAssignments = KUtil.findFileIdentifiers(project, virtualFile);
    List<TreeElement> children = new ArrayList<>(topLevelAssignments.size());
    for (KUserId topLevelAssignment : topLevelAssignments) {
      children.add(new KStructureViewElement(topLevelAssignment));
    }
    return children.toArray(new TreeElement[children.size()]);
  }

  @Override
  public void navigate(boolean b) {
    if (element instanceof NavigationItem) {
      ((NavigationItem)element).navigate(b);
    }
  }

  @Override
  public boolean canNavigate() {
    return element instanceof NavigationItem && ((NavigationItem)element).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return element instanceof NavigationItem && ((NavigationItem)element).canNavigateToSource();
  }

  @Override
  public int getAccessLevel() {
    return 0;
  }

  @Override
  public int getSubLevel() {
    return 0;
  }

  private static ItemPresentation getTreeItemPresentation(final KUserId element) {
    final Optional<KLambda> fnDefinition = KUtil.getFunctionDefinition(element);
    return new ColoredItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return KUtil.getDescriptiveName(element);
      }

      @Nullable
      @Override
      public String getLocationString() {
        return null;
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        final String name = element.getName();
        if (name.startsWith("i.") || name.contains(".i.")) {
          return AllIcons.Nodes.C_private;
        }
        return AllIcons.Nodes.C_public;
      }

      @Nullable
      @Override
      public TextAttributesKey getTextAttributesKey() {
        final String name = element.getName();
        final String ns = KUserIdCache.getExplicitNamespace(name);
        final String localName = ns != null ? name.substring(ns.length() + 1) : name;
        if (fnDefinition.isPresent() && localName.charAt(0) == '_') { // side-effect function
          return KSyntaxHighlighter.IDENTIFIER_SYS;
        }
        return null;
      }
    };
  }
}
