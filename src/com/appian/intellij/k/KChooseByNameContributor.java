package com.appian.intellij.k;

import org.jetbrains.annotations.NotNull;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;

public final class KChooseByNameContributor implements ChooseByNameContributor {

  @NotNull
  @Override
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    return KUserIdCache.getInstance().getAllNames(project);
  }

  @NotNull
  @Override
  public NavigationItem[] getItemsByName(
      String name, String pattern, Project project, boolean includeNonProjectItems) {
    return KUserIdCache.getInstance().getByName(project, pattern);
  }

}
