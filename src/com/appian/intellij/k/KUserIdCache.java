package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;

/**
 * Caches K functions information per file.
 */
final class KUserIdCache extends VirtualFileAdapter {

  private static final KUserIdCache INSTANCE = new KUserIdCache();
  public static final KUserIdCache getInstance() {
    return INSTANCE;
  }

  // if any k file has been touched
  private final Map<String, Collection<KUserId>> idCache = new HashMap<>();
  private final Map<String, String[]> nameCache = new HashMap<>();

  private KUserIdCache() {}

  public String[] getAllNames(Project project) {
    return findNames(project);
  }

  private String[] findNames(Project project) {
    final VirtualFile[] files = FileBasedIndex.getInstance()
      .getContainingFiles(FileTypeIndex.NAME, KFileType.INSTANCE, GlobalSearchScope.allScope(project))
      .toArray(new VirtualFile[0]);
    final String[][] items = new String[files.length][];
    for(int i = 0; i < files.length; i++) {
      final String filePath = files[i].getPath();
      String[] kUserIds = nameCache.get(filePath);
      if (kUserIds == null) {
        kUserIds = KUtil.findIdentifiers(project, files[i])
          .map(KUserId::getName)
          .toArray(size -> new String[size]);
        nameCache.put(filePath, kUserIds);
      }
      items[i] = kUserIds;
    }
    return concat(items);
  }

  public NavigationItem[] getByName(Project project, String pattern) {
    return findByName(project, pattern);
  }

  private NavigationItem[] findByName(Project project, String pattern) {
    final VirtualFile[] files = FileBasedIndex.getInstance()
      .getContainingFiles(FileTypeIndex.NAME, KFileType.INSTANCE, GlobalSearchScope.allScope(project))
      .toArray(new VirtualFile[0]);
    final NavigationItem[][] items = new NavigationItem[files.length][];
    for(int i = 0; i < files.length; i++) {
      final String filePath = files[i].getPath();
      Collection<KUserId> kUserIds = idCache.get(filePath);
      if (kUserIds == null) {
        kUserIds = KUtil.findIdentifiers(project, files[i])
          .collect(Collectors.toList());
        idCache.put(filePath, kUserIds);
      }
      items[i] = filter(kUserIds, pattern);
    }
    return concat(items);
  }

  private NavigationItem[] filter(Collection<KUserId> kUserIds, String pattern) {
    return kUserIds.stream()
      .filter(id -> id.getText().startsWith(pattern))
      .toArray(size -> new NavigationItem[size]);
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    removeEntry(event);
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    removeEntry(event);
  }

  @Override
  public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    removeEntry(event);
  }

  private void removeEntry(@NotNull VirtualFileEvent event) {
    final VirtualFile file = event.getFile();
    if (KFileType.INSTANCE != file.getFileType()) {
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

  private static <T> T[] concat(T[]... input) {
    final T[] first = input[0];
    int totalLength = first.length;
    for (int i = 1; i < input.length; i++) {
      totalLength += input[i].length;
    }
    T[] result = Arrays.copyOf(first, totalLength);
    int offset = first.length;
    for (int i = 1; i < input.length; i++) {
      System.arraycopy(input[i], 0, result, offset, input[i].length);
      offset += input[i].length;
    }
    return result;
  }

}
