package com.appian.intellij.k.settings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KSettings {
  private List<String> internalPrefixes = Collections.singletonList(".i.");

  @SuppressWarnings("WeakerAccess") // needs to be public property to be persistable
  public String getInternalPrefixes() {
    return String.join("; ", internalPrefixes);
  }

  @SuppressWarnings("WeakerAccess") // needs to be public property to be persistable
  public void setInternalPrefixes(String internalPrefixes) {
    if (internalPrefixes == null || internalPrefixes.trim().isEmpty())
      this.internalPrefixes = Collections.emptyList();
    else
      this.internalPrefixes = Stream.of(internalPrefixes.split(";"))
          .map(String::trim)
          .filter(s->!s.isEmpty())
          .collect(Collectors.toList());
  }

  public boolean isInternalName(String name) {
    return internalPrefixes.stream().anyMatch(name::startsWith);
  }
}
