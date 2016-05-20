package com.baulsupp.oksocial.secrets;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class Secrets {
  private static Secrets instance;

  private final Map<String, String> secrets;

  public Secrets(Map<String, String> secrets) {
    this.secrets = secrets;
  }

  public Optional<String> get(String key) {
    return Optional.ofNullable(secrets.get(key));
  }

  public static Secrets loadSecrets() {
    Properties p = new Properties();
    try {
      p.load(Secrets.class.getResourceAsStream("/oksocial-secrets.properties"));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    Secrets secrets = new Secrets(new HashMap(p));

    return secrets;
  }

  public static Optional<String> getDefined(String key) {
    if (instance == null) {
      instance = loadSecrets();
    }

    return instance.get(key);
  }

  public static String prompt(String name, String key, boolean password) {
    Optional<String> defaultValue = getDefined(key);

    String prompt = name + defaultDisplay(defaultValue, password) + ": ";

    String value = "";

    if (System.console() != null) {
      if (password) {
        value = new String(System.console().readPassword(prompt));
      } else {
        value = System.console().readLine(prompt);
      }
    }

    if (value.isEmpty()) {
      value = defaultValue.orElse("");
    }

    return value;
  }

  private static String defaultDisplay(Optional<String> defaultValue, boolean password) {
    if (password) {
      defaultValue = defaultValue.map(s -> s.replaceAll(".", "\\*"));
    }

    return defaultValue.map(s -> " [" + s + "]").orElse("");
  }
}
