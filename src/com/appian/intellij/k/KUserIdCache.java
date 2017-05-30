package com.appian.intellij.k;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManagerListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * Caches K functions information per file.
 */
public final class KUserIdCache extends VirtualFileAdapter implements VirtualFileManagerListener {

  private static final KUserIdCache INSTANCE = new KUserIdCache();
  public static final KUserIdCache getInstance() {
    return INSTANCE;
  }

  private final Map<String, Collection<KUserId>> idCache = new HashMap<>();
  private final Map<String, Collection<String>> nameCache = new HashMap<>();

  private KUserIdCache() {}

  public String[] getAllNames(Project project) {
    final String[] names = findNames(project);
    return names;
  }

  private String[] findNames(Project project) {
    final Collection<VirtualFile> files = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    return files.stream().flatMap(file -> {
      final String filePath = file.getPath();
      final Collection<String> kUserIds = nameCache.computeIfAbsent(filePath,
          k -> KUtil.findFileIdentifiers(project, file)
              .stream()
              .map(KUserId::getName)
              .collect(Collectors.toList()));
      return kUserIds.stream();
    }).toArray(String[]::new);
  }

  public NavigationItem[] getByName(Project project, String pattern) {
    final NavigationItem[] byName = findByName(project, pattern);
    return byName;
  }

  private NavigationItem[] findByName(final Project project, final String pattern) {
    final Collection<VirtualFile> files = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(project));
    return files.stream().flatMap(file -> {
      final String filePath = file.getPath();
      final Collection<KUserId> kUserIds = idCache.computeIfAbsent(filePath,
          k -> KUtil.findFileIdentifiers(project, file));
      return filter(kUserIds, pattern);
    }).toArray(NavigationItem[]::new);
  }

  private Stream<KUserId> filter(Collection<KUserId> kUserIds, String pattern) {
    return kUserIds.stream().filter(id -> id.getName().contains(pattern));
  }

  @Nullable
  public static String getExplicitNamespace(String fnName) {
    return fnName.charAt(0) == '.' ? fnName.substring(0, fnName.lastIndexOf('.')) : null;
  }

  static String generateFqn(String namespace, String fnName) {
    return namespace + "." + fnName;
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    handleFileEvent(event);
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    handleFileEvent(event);
  }

  @Override
  public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    handleFileEvent(event);
  }

  private void handleFileEvent(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  void remove(VirtualFile file) {
    if (file == null || KFileType.INSTANCE != file.getFileType()) {
      return;
    }
    final String filePath = file.getPath();
    idCache.remove(filePath);
    nameCache.remove(filePath);
  }

  final KUserIdCache clear() {
    idCache.clear();
    nameCache.clear();
    return this;
  }

  @Override
  public void beforeRefreshStart(boolean asynchronous) {
    System.out.println();
  }

  @Override
  public void afterRefreshFinish(boolean asynchronous) {
    System.out.println();
  }

}
