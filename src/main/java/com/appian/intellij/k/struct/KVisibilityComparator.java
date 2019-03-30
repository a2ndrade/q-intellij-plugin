package com.appian.intellij.k.struct;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KNamedElement;
import com.appian.intellij.k.psi.KUserId;

public class KVisibilityComparator implements Comparator {
  public static final Comparator INSTANCE = new KVisibilityComparator(null);

  private final Comparator nextComparator;

  public KVisibilityComparator(Comparator comparator) {
    nextComparator = comparator;
  }

  @Override
  public int compare(@NotNull Object descriptor1, @NotNull Object descriptor2) {
    int accessLevel1 = getAccessLevel(descriptor1);
    int accessLevel2 = getAccessLevel(descriptor2);
    if (accessLevel1 == accessLevel2 && nextComparator != null) {
      //noinspection unchecked
      return nextComparator.compare(descriptor1, descriptor2);
    }
    return accessLevel2 - accessLevel1;
  }

  private static int getAccessLevel(@NotNull Object element) {
    if (element instanceof KStructureViewElement) {
      return ((KStructureViewElement)element).getAccessLevel();
    }

    if (element instanceof KUserId) {
      return ((KUserId)element).getAccessLevel();
    }
    return KNamedElement.UNKNOWN_ACCESS_LEVEL;
  }
}
