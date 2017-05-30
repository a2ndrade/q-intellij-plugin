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
    final KUserIdCache cache = KUserIdCache.getInstance().clear();
    final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
    virtualFileManager.addVirtualFileListener(cache);
    virtualFileManager.addVirtualFileManagerListener(cache);
  }

  @Override
  public void disposeComponent() {
    final KUserIdCache cache = KUserIdCache.getInstance().clear();
    final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
    virtualFileManager.removeVirtualFileListener(cache);
    virtualFileManager.removeVirtualFileManagerListener(cache);
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "k-intellij-plugin";
  }

}
