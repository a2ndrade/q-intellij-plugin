package com.appian.intellij.k;

import javax.swing.Icon;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.RowIcon;

public final class KIcons {
  public static final Icon FILE = IconLoader.getIcon("q.png");

  public static final Icon PUBLIC_FUNCTION = new RowIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_FUNCTION = new RowIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_private);

  public static final Icon PUBLIC_VARIABLE = new RowIcon(AllIcons.Nodes.Variable, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_VARIABLE = new RowIcon(AllIcons.Nodes.Variable, AllIcons.Nodes.C_private);
}
