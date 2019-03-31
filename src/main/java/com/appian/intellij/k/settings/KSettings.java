package com.appian.intellij.k.settings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class KSettings {
  private List<String> internalPrefixes = Collections.singletonList("i.");
  private List<String> internalSubstrings = Collections.singletonList(".i.");

  @SuppressWarnings("WeakerAccess") // needs to be public property to be persistable
  public String getInternalPrefixes() {
    return semiJoin(this.internalPrefixes);
  }

  @SuppressWarnings("WeakerAccess") // needs to be public property to be persistable
  public void setInternalSubstrings(String internalSubstrings) {
    this.internalSubstrings = semiSplit(internalSubstrings);
  }

  @SuppressWarnings("WeakerAccess") // needs to be public property to be persistable
  public String getInternalSubstrings() {
    return semiJoin(this.internalSubstrings);
  }

  @SuppressWarnings("WeakerAccess") // needs to be public property to be persistable
  public void setInternalPrefixes(String internalPrefixes) {
    this.internalPrefixes = semiSplit(internalPrefixes);
  }

  public boolean isInternalName(String name) {
    return internalPrefixes.stream().anyMatch(name::startsWith) || internalSubstrings.stream().anyMatch(name::contains);
  }

  @NotNull
  private static String semiJoin(List<String> string) {
    return String.join("; ", string);
  }

  @NotNull
  private static List<String> semiSplit(String string) {
    if (string == null || string.trim().isEmpty()) {
      return Collections.emptyList();
    }

    return Stream.of(string.split(";"))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }
}
