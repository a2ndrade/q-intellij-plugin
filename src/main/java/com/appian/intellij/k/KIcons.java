package com.appian.intellij.k;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;

public final class KIcons {
  public static final Icon FILE = IconLoader.getIcon("q.png");
  public static final Icon KEY = IconLoader.getIcon("key.png");

  public static final Icon PUBLIC_FUNCTION = new RowIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_FUNCTION = new RowIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_private);

  public static final Icon PUBLIC_VARIABLE = new RowIcon(AllIcons.Nodes.Variable, AllIcons.Nodes.C_public);
  public static final Icon PRIVATE_VARIABLE = new RowIcon(AllIcons.Nodes.Variable, AllIcons.Nodes.C_private);

  public static final Icon QSERVER =  layer(AllIcons.Webreferences.Server, FILE);
  public static final Icon QAUTHDRIVER =  layer(FILE, KEY);

  public static final Icon RUN_SELECTION = AllIcons.RunConfigurations.TestState.Run;
  public static final Icon DEFINE_SELECTION = AllIcons.Actions.Upload;

  @SuppressWarnings("SameParameterValue")
  @NotNull
  private static LayeredIcon layer(Icon icon1, Icon icon2) {
    LayeredIcon icon = new LayeredIcon(2);
    icon.setIcon(icon1, 0, 0, 0);
    icon.setIcon(icon2, 1, 6, 6);
    return icon;
  }

  @SuppressWarnings("WeakerAccess")
  public static final Icon SYSTEM_FUNCTION = FILE;
}
