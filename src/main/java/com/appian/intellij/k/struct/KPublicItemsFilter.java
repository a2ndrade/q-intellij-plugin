package com.appian.intellij.k.struct;

import com.appian.intellij.k.psi.KNamedElement;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.util.PlatformIcons;

import org.jetbrains.annotations.NotNull;

public class KPublicItemsFilter implements Filter {

  @Override
  public boolean isVisible(TreeElement treeNode) {
    if (treeNode instanceof KStructureViewElement) {
      return ((KStructureViewElement)treeNode).getAccessLevel() != KNamedElement.PRIVATE_ACCESS_LEVEL;
    } else {
      return true;
    }
  }

  @Override
  @NotNull
  public ActionPresentation getPresentation() {
    return new ActionPresentationData(IdeBundle.message("action.structureview.show.non.public"), null,
        PlatformIcons.PRIVATE_ICON);
  }

  @Override
  @NotNull
  public String getName() {
    return "K_SHOW_NON_PUBLIC";
  }

  @Override
  public boolean isReverted() {
    return true;
  }
}
