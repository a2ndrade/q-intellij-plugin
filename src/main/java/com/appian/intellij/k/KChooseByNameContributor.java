package com.appian.intellij.k;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;

public final class KChooseByNameContributor implements ChooseByNameContributor {

  @NotNull
  @Override
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    final KUserIdCache cache = KUserIdCache.getInstance();
    final Stream<VirtualFile> files = FileTypeIndex.getFiles(KFileType.INSTANCE, GlobalSearchScope.allScope(project))
        .stream();
    return files.flatMap(file -> Stream.of(cache.getIdentifiers(project, file))).toArray(String[]::new);
  }

  @NotNull
  @Override
  public NavigationItem[] getItemsByName(
      String name, String pattern, Project project, boolean includeNonProjectItems) {
    final KUserIdCache cache = KUserIdCache.getInstance();
    final Stream<VirtualFile> files = FileTypeIndex.getFiles(KFileType.INSTANCE, GlobalSearchScope.allScope(project))
        .stream();
    return files.flatMap(
        file -> cache.findIdentifiers(project, file, name, true, new KUtil.ExactMatcher(name)).stream())
        .toArray(NavigationItem[]::new);
  }

}
