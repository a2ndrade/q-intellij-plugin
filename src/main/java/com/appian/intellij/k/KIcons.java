package com.appian.intellij.k;

import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.RowIcon;

public final class KIcons {
  public static final Icon FILE = IconLoader.getIcon("q.png");
  public static final Icon FUNCTION = IconLoader.getIcon("f.png");
  public static final Icon VARIABLE = IconLoader.getIcon("v.png");

  public static final Icon PUBLIC_FUNCTION = new RowIcon(FUNCTION, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_FUNCTION = new RowIcon(FUNCTION, AllIcons.Nodes.C_private);

  public static final Icon PUBLIC_VARIABLE = new RowIcon(VARIABLE, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_VARIABLE = new RowIcon(VARIABLE, AllIcons.Nodes.C_private);
}
