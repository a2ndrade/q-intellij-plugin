package com.appian.intellij.k.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class KSettings implements Cloneable {
  private List<String> internalPrefixes = Collections.singletonList("i.");
  private List<String> internalSubstrings = Collections.singletonList(".i.");
  private List<KServerSpec> servers = new ArrayList<>();

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

  public List<KServerSpec> getServers() {
    return servers;
  }

  public void setServers(List<KServerSpec> servers) {
    this.servers = servers;
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

    return Stream.of(string.split(";")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
  }

  public KSettings clone() {
    try {
      return (KSettings)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
