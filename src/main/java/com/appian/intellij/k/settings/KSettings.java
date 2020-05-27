package com.appian.intellij.k.settings;

import static com.appian.intellij.k.settings.KAuthDriverSpec.getBasicAuthenticator;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KSettings implements Cloneable {
  private List<String> internalPrefixes = singletonList("i.");
  private List<String> internalSubstrings = singletonList(".i.");
  private List<KServerSpec> servers = new ArrayList<>();
  private List<KAuthDriverSpec> authDrivers = new ArrayList<>();

  @NotNull
  public KSettings cloneWithNewServer(KServerSpec newServer) {
    List<KServerSpec> newServers = new ArrayList<>(getServers());
    newServers.add(newServer);
    KSettings newSettings = clone();
    newSettings.setServers(newServers);
    return newSettings;
  }

  @NotNull
  public KSettings cloneWithNewAuthDriver(KAuthDriverSpec newAuthDriver) {
    List<KAuthDriverSpec> newDrivers = new ArrayList<>(getAuthDrivers());
    newDrivers.add(newAuthDriver);
    KSettings newSettings = clone();
    newSettings.setAuthDrivers(newDrivers);
    return newSettings;
  }


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

  public List<KAuthDriverSpec> getAuthDrivers() {
    return authDrivers;
  }

  public void setAuthDrivers(List<KAuthDriverSpec> authDrivers) {
    this.authDrivers = requireNonNull(authDrivers);
  }

  public Optional<KAuthDriverSpec> findAuthDriver(String name) {
    return authDrivers.stream().filter(e -> name.equals(e.getName())).findFirst();
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

    return Stream.of(string.split(";")).map(String::trim).filter(s -> !s.isEmpty()).collect(toList());
  }

  public KSettings clone() {
    try {
      return (KSettings)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public Function<String, String> getAuthenticator(@Nullable String authDriverName) {
    if (authDriverName == null) {
      return getBasicAuthenticator();
    }

    return findAuthDriver(authDriverName)
        .map(KAuthDriverSpec::newAuthenticator)
        .orElseThrow(() -> new RuntimeException("Configuration error: authentication driver " + authDriverName + " is not defined"));
  }
}
