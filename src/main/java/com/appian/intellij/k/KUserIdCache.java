package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import com.appian.intellij.k.psi.KNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.psi.PsiFile;

/**
 * Caches K functions information per file.
 */
public final class KUserIdCache implements VirtualFileListener {

  static final Key<String[]> USER_IDS = Key.create("userIds");
  static final Key<Trie<Boolean>> USER_IDS_TRIE = Key.create("userIdsTrie");

  private static final KUserIdCache INSTANCE = new KUserIdCache();
  public static KUserIdCache getInstance() {
    return INSTANCE;
  }

  private KUserIdCache() {}

  String[] getIdentifiers(Project project, VirtualFile file) {
    String[] userIds = file.getUserData(USER_IDS);
    if (userIds == null) {
      userIds = KUtil.findIdentifiers(project, file)
          .stream()
          .map(KUtil::getFqnOrName)
          .sorted()
          .toArray(String[]::new);
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
        userIds.put(KUtil.getFqnOrName(userId), Boolean.TRUE);
      }
      file.putUserData(USER_IDS_TRIE, userIds);
    }
    return userIds;
  }

  @Nullable
  KUserId findFirstExactMatch(Project project, VirtualFile file, String targetIdentifier) {
    final Iterator<KUserId> it = findIdentifiers(project, file, targetIdentifier, true,
        new KUtil.ExactMatcher(targetIdentifier)).iterator();
    return it.hasNext() ? it.next() : null;
  }

  @NotNull
  Collection<KUserId> findAllIdentifiers(
      Project project, VirtualFile file, String targetIdentifier, KUtil.Matcher matcher) {
    return findIdentifiers(project, file, targetIdentifier, false, matcher);
  }

  @NotNull
  Collection<KUserId> findIdentifiers(
      Project project,
      VirtualFile file,
      String targetIdentifier,
      boolean stopAfterFirstMatch,
      KUtil.Matcher matcher) {
    if (matcher instanceof KUtil.ExactMatcher) {
      final String[] userIds = getIdentifiers(project, file);
      if (Arrays.binarySearch(userIds, targetIdentifier) < 0) {
        return Collections.emptyList();
      }
    } else if (matcher instanceof KUtil.PrefixMatcher) {
      final Trie<Boolean> userIds = getIdentifiersTrie(project, file);
      if (!userIds.containsKeyWithPrefix(targetIdentifier)) {
        return Collections.emptyList();
      }
    }
    return KUtil.findIdentifiers(project, file, matcher, stopAfterFirstMatch);
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  public void remove(KNamedElement target) {
    remove(Optional.of(target).map(KNamedElement::getContainingFile).map(PsiFile::getVirtualFile).orElse(null));
  }

  void remove(VirtualFile file) {
    if (file == null || KFileType.INSTANCE != file.getFileType()) {
      return;
    }
    file.putUserData(USER_IDS, null);
    file.putUserData(USER_IDS_TRIE, null);
  }

}
