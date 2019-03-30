package com.appian.intellij.k.struct;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.KPluginBundle;
import com.appian.intellij.k.KUtil;
import com.appian.intellij.k.psi.KNamedElement;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;

public class KGlobalsFilter implements Filter {

  @Override
  public boolean isVisible(TreeElement treeNode) {
    if (!(treeNode instanceof KStructureViewElement)) {
      return false;
    }

    KStructureViewElement e = (KStructureViewElement)treeNode;

    return (e.getValue() instanceof KNamedElement) &&
        KUtil.getFunctionDefinition(((KNamedElement)e.getValue())).isPresent();
  }

  @Override
  @NotNull
  public ActionPresentation getPresentation() {
    return new ActionPresentationData(KPluginBundle.message("action.structureview.show.globals"), null,
        AllIcons.Nodes.Variable);
  }

  @Override
  @NotNull
  public String getName() {
    return "K_SHOW_GLOBALS";
  }

  @Override
  public boolean isReverted() {
    return true;
  }
}
