package com.baulsupp.oksocial.secrets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import static com.baulsupp.oksocial.util.Util.or;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class Secrets {
  private static Secrets instance;

  private final Map<String, String> secrets;
  private final Function<String, Optional<String>> defaults;
  private final Optional<Path> file;
  private boolean changed = false;

  public Secrets(Map<String, String> secrets, Optional<Path> file,
      Function<String, Optional<String>> defaults) {
    this.secrets = secrets;
    this.defaults = defaults;
    this.file = file;
  }

  public Optional<String> get(String key) {
    Optional<String> result = ofNullable(secrets.get(key));

    if (!result.isPresent()) {
      result = defaults.apply(key);
    }

    return result.filter(s -> !s.isEmpty());
  }

  private void put(String key, String value) {
    secrets.put(key, value);
    changed = true;
  }

  public static Secrets loadSecrets() {
    Secrets classPathSecrets = loadClasspathDefaults();

    Path configFile =
        FileSystems.getDefault().getPath(System.getenv("HOME"), ".oksocial-secrets.properties");

    Properties p = new Properties();
    if (Files.exists(configFile)) {
      try (BufferedReader r = Files.newBufferedReader(configFile)) {
        p.load(r);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return new Secrets(new HashMap(p), Optional.of(configFile), classPathSecrets::get);
  }

  public static Secrets loadClasspathDefaults() {
    Properties p = new Properties();

    try (InputStream is = Secrets.class.getResourceAsStream("/oksocial-secrets.properties")) {
      if (is != null) {
        p.load(is);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new Secrets(new HashMap(p), empty(), k -> empty());
  }

  public static Optional<String> getDefined(String key) {
    return instance().get(key);
  }

  public static String prompt(String name, String key, String defaultValue, boolean password) {
    Optional<String> defaulted = or(instance().getDefined(key), () -> ofNullable(defaultValue));

    String prompt = name + defaultDisplay(defaulted, password) + ": ";

    String value = "";

    if (System.console() != null) {
      if (password) {
        value = new String(System.console().readPassword(prompt));
      } else {
        value = System.console().readLine(prompt);
      }
    } else {
      System.err.println("using default value for " + key);
    }

    if (value.isEmpty()) {
      value = defaulted.orElse("");
    } else {
      instance().put(key, value);
    }

    return value;
  }

  public static Set<String> promptArray(String name, String key, Collection<String> defaults) {
    String valueString =
        prompt(name, key, defaults.stream().collect(joining(",")), false);
    return newHashSet(asList(valueString.split(",")));
  }

  private static String defaultDisplay(Optional<String> defaultValue, boolean password) {
    if (password) {
      defaultValue = defaultValue.map(s -> s.replaceAll(".", "\\*"));
    }

    return defaultValue.map(s -> " [" + s + "]").orElse("");
  }

  public static synchronized Secrets instance() {
    if (instance == null) {
      instance = loadSecrets();
    }

    return instance;
  }

  public void saveIfNeeded() throws IOException {
    if (changed && file.isPresent()) {
      Properties p = new Properties();
      p.putAll(secrets);

      try (Writer w = Files.newBufferedWriter(file.get())) {
        p.store(w, null);
      }
    }
  }
}
