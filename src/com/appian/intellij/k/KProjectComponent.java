package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.vfs.VirtualFileManager;

public final class KProjectComponent implements ProjectComponent {

  public KProjectComponent(){}

  @Override
  public void projectOpened() {
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void initComponent() {
    VirtualFileManager.getInstance().addVirtualFileListener(KUserIdCache.getInstance());
  }

  @Override
  public void disposeComponent() {
    VirtualFileManager.getInstance().removeVirtualFileListener(KUserIdCache.getInstance());
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "k-intellij-plugin";
  }

}
