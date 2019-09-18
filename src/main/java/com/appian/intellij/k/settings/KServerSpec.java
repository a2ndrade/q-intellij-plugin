package com.appian.intellij.k.settings;

import java.util.Objects;

import com.intellij.util.xmlb.annotations.Transient;

public class KServerSpec {
  private String name;
  private String host;
  private int port;
  private boolean useTLS;
  private String user;
  private String password;

  @SuppressWarnings("unused") //required for serialization
  public KServerSpec() {
  }

  KServerSpec(String name, String host, int port, boolean useTLS, String user, String password) {
    this.name = Objects.requireNonNull(name);
    this.host = Objects.requireNonNull(host);
    this.port = port;
    this.useTLS = useTLS;
    this.user = user;
    this.password = password;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public boolean useTLS() {
    return useTLS;
  }

  @Transient // do not persist in plain text xml
  public String getUser() {
    return user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Transient // do not persist in plain text xml
  public String getPassword() {
    return password;
  }

  public String getId() {
    return (name == null ? "" : name) + " [" + (user == null || user.isEmpty() ? "" : user + "@") + host + ":" + port + "]";
  }

  public String toString() {
    return getId();
  }

  @SuppressWarnings("unused") // needed for serialization
  public void setHost(String host) {
    this.host = Objects.requireNonNull(host);
  }

  @SuppressWarnings("unused") // needed for serialization
  public void setPort(int port) {
    this.port = port;
  }

  @SuppressWarnings("unused") // needed for serialization
  public void setUseTLS(boolean useTLS) {
    this.useTLS = useTLS;
  }

  @SuppressWarnings({"unused", "WeakerAccess"}) // needed for serialization
  public void setUser(String user) {
    this.user = user;
  }

  @SuppressWarnings({"unused", "WeakerAccess"}) // needed for serialization
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KServerSpec that = (KServerSpec)o;
    return Objects.equals(name, that.name) && port == that.port && useTLS == that.useTLS && host.equals(that.host) &&
        Objects.equals(user, that.user) && Objects.equals(password, that.password);
  }
}
