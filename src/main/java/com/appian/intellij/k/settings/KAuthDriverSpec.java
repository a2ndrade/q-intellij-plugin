package com.appian.intellij.k.settings;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

public class KAuthDriverSpec {
  private String name;
  private String className;
  private List<String> jars;

  @SuppressWarnings("unused") // required for serialization
  public KAuthDriverSpec() {
  }

  public KAuthDriverSpec(String name, String className, List<String> jars) {
    this.name = requireNonNull(name);
    this.className = requireNonNull(className);
    this.jars = requireNonNull(jars);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public List<String> getJars() {
    return jars;
  }

  public void setJars(List<String> jars) {
    this.jars = jars;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KAuthDriverSpec that = (KAuthDriverSpec)o;
    return Objects.equals(name, that.name) && Objects.equals(className, that.className) && Objects.equals(jars, that.jars);
  }

  @Override
  public int hashCode() {
    return Objects.hash(className, jars);
  }

  private static final Map<String[],ClassLoader> classLoaders = new HashMap<>();

  public Function<String, String> newAuthenticator() {
    String[] jarPaths = getJars().toArray(new String[0]);
    ClassLoader classLoader = classLoaders.computeIfAbsent(jarPaths, p->createClassLoader(jarPaths));
    try {
      Class<?> clazz = classLoader.loadClass(getClassName());
      // noinspection unchecked
      return (Function<String, String>) clazz.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private ClassLoader createClassLoader(String[] jarPaths) {
    try {
      URL[] urls = new URL[jarPaths.length];
      for (int i = 0; i < urls.length; ++i) {
        urls[i] = new File(jarPaths[i]).toURI().toURL();
      }
      return  new URLClassLoader(urls);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return getName() + " (" + getClassName() + ")";
  }

  public static Function<String, String> getBasicAuthenticator() {
    return (connectionString) -> {
      String[] userHost = connectionString.split("@", -1);
      if (userHost.length != 2)
        throw newInvalidConnectionStringFormatException();

      String[] userPassword = userHost[0].split(":", -1);
      if (userPassword.length != 2)
        throw newInvalidConnectionStringFormatException();

      return userHost[0];
    };
  }

  private static  RuntimeException newInvalidConnectionStringFormatException() {
    return new RuntimeException("Connection string must be in the format user:password@host:port");
  }
}

