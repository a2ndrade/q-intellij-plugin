package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KUserId;
import com.google.common.base.Strings;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;


final class KStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

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
    return element instanceof NavigationItem ? ((NavigationItem)element).getPresentation() : null;
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
}
