package com.appian.intellij.k;

import javax.swing.Icon;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;

public final class KIcons {
  public static final Icon FILE = IconLoader.getIcon("q.png");

  public static final Icon PUBLIC_FUNCTION = new RowIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_FUNCTION = new RowIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_private);

  public static final Icon PUBLIC_VARIABLE = new RowIcon(AllIcons.Nodes.Variable, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_VARIABLE = new RowIcon(AllIcons.Nodes.Variable, AllIcons.Nodes.C_private);

  public static final Icon QSERVER;

  static {
    LayeredIcon icon = new LayeredIcon(2);
    icon.setIcon(AllIcons.Webreferences.Server, 0, 0, 0);
    icon.setIcon(FILE, 1, 6, 6);
    QSERVER = icon;
  }

  public static final Icon SYSTEM_FUNCTION = FILE;
}
