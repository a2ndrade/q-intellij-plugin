package com.appian.intellij.k.struct;

import java.util.Comparator;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Sorter;

public class KVisibilitySorter implements Sorter {
  public static final Sorter INSTANCE = new KVisibilitySorter();

  private static final ActionPresentation PRESENTATION = new ActionPresentation() {
    @Override
    @NotNull
    public String getText() {
      return IdeBundle.message("action.structureview.sort.by.visibility");
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Icon getIcon() {
      return AllIcons.ObjectBrowser.VisibilitySort;
    }
  };

  @Override
  @NotNull
  public Comparator getComparator() {
    return KVisibilityComparator.INSTANCE;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  @NotNull
  public ActionPresentation getPresentation() {
    return PRESENTATION;
  }

  @Override
  @NotNull
  public String getName() {
    return "K_VISIBILITY_SORTER";
  }
}
