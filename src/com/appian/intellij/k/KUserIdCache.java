package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;

/**
 * Caches K functions information per file.
 */
final class KUserIdCache extends VirtualFileAdapter {

  static final Key<String[]> USER_IDS = Key.create("userIds");
  static final Key<Trie<Boolean>> USER_IDS_TRIE = Key.create("userIdsTrie");

  private static final KUserIdCache INSTANCE = new KUserIdCache();
  public static final KUserIdCache getInstance() {
    return INSTANCE;
  }

  private KUserIdCache() {}

  String[] getIdentifiers(Project project, VirtualFile file) {
    String[] userIds = file.getUserData(USER_IDS);
    if (userIds == null) {
      userIds = KUtil.findIdentifiers(project, file).stream().map(KUserId::getName).toArray(String[]::new);
      file.putUserData(USER_IDS, userIds);
    }
    return userIds;
  }

  private Trie<Boolean> getIdentifiersTrie(Project project, VirtualFile file) {
    Trie<Boolean> userIds = file.getUserData(USER_IDS_TRIE);
    if (userIds == null) {
      userIds = new Trie<>();
      final Collection<KUserId> identifiers = KUtil.findIdentifiers(project, file);
      for (KUserId userId : identifiers) {
        userIds.put(userId.getName(), Boolean.TRUE);
      }
      file.putUserData(USER_IDS_TRIE, userIds);
    }
    return userIds;
  }

  @NotNull
  Collection<KUserId> findAllIdentifiers(
      Project project, VirtualFile file, String targetIdentifier, boolean exactMatch) {
    return findIdentifiers(project, file, targetIdentifier, false, exactMatch);
  }

  @NotNull
  Collection<KUserId> findIdentifiers(
      Project project,
      VirtualFile file,
      String targetIdentifier,
      boolean stopAfterFirstMatch,
      boolean exactMatch) {
    if (exactMatch) {
      final String[] userIds = getIdentifiers(project, file);
      if (Arrays.binarySearch(userIds, targetIdentifier) < 0) {
        return Collections.emptyList();
      }
    } else {
      final Trie<Boolean> userIds = getIdentifiersTrie(project, file);
      if (!userIds.containsKeyWithPrefix(targetIdentifier)) {
        return Collections.emptyList();
      }
    }
    return KUtil.findIdentifiers(project, file, targetIdentifier, stopAfterFirstMatch, exactMatch);
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  void remove(VirtualFile file) {
    if (file == null || KFileType.INSTANCE != file.getFileType()) {
      return;
    }
    file.putUserData(USER_IDS, null);
  }

}
