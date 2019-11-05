package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KUserId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.psi.PsiElement;
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

  private KUserIdCache() {
  }

  String[] getIdentifiers(Project project, VirtualFile file) {
    String[] userIds = file.getUserData(USER_IDS);
    if (userIds == null) {
      userIds = KUtil.findGlobalDeclarations(project, file).stream().map(KUtil::getFqnOrName).sorted().toArray(String[]::new);
      file.putUserData(USER_IDS, userIds);
    }
    return userIds;
  }

  private Trie<Boolean> getIdentifiersTrie(Project project, VirtualFile file) {
    Trie<Boolean> userIds = file.getUserData(USER_IDS_TRIE);
    if (userIds == null) {
      userIds = new Trie<>();
      final Collection<KUserId> identifiers = KUtil.findGlobalDeclarations(project, file);
      for (KUserId userId : identifiers) {
        userIds.put(KUtil.getFqnOrName(userId), Boolean.TRUE);
      }
      file.putUserData(USER_IDS_TRIE, userIds);
    }
    return userIds;
  }

  @Nullable
  KUserId findFirstExactMatch(Project project, VirtualFile file, String targetIdentifier) {
    final Iterator<KUserId> it = findGlobalDeclarations(project, file, true,
        new KUtil.ExactGlobalAssignmentMatcher(targetIdentifier)).iterator();
    return it.hasNext() ? it.next() : null;
  }

  @NotNull
  Collection<KUserId> findGlobalDeclarations(
      Project project, VirtualFile file, KUtil.Matcher matcher) {
    return findGlobalDeclarations(project, file, false, matcher);
  }

  @NotNull
  Collection<KUserId> findGlobalDeclarations(
      Project project, VirtualFile file, boolean stopAfterFirstMatch, KUtil.Matcher matcher) {
    final String targetIdentifier = matcher.getTarget();
    // quickly skip files without a potential match
    if (!matcher.skipCacheCheck()) {
      if (matcher instanceof KUtil.ExactGlobalAssignmentMatcher) {
        final String[] userIds = getIdentifiers(project, file);
        if (Arrays.binarySearch(userIds, targetIdentifier) < 0) {
          return Collections.emptyList();
        }
      } else if (matcher instanceof KUtil.PrefixGlobalAssignmentMatcher) {
        final Trie<Boolean> userIds = getIdentifiersTrie(project, file);
        if (!userIds.containsKeyWithPrefix(targetIdentifier)) {
          return Collections.emptyList();
        }
      }
    }
    return KUtil.findGlobalDeclarations(project, file, matcher, stopAfterFirstMatch);
  }

  @Override
  public void beforeContentsChange(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    remove(event.getFile());
  }

  public void remove(PsiElement target) {
    remove(Optional.of(target).map(PsiElement::getContainingFile).map(PsiFile::getVirtualFile).orElse(null));
  }

  void remove(VirtualFile file) {
    if (file == null || KFileType.INSTANCE != file.getFileType()) {
      return;
    }
    file.putUserData(USER_IDS, null);
    file.putUserData(USER_IDS_TRIE, null);
  }

}
