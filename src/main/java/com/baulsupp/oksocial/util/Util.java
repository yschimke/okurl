package com.baulsupp.oksocial.util;

import com.baulsupp.oksocial.Main;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Util {
  private Util() {
  }

  public static String versionString(String propertiesFile) {
    try {
      Properties prop = new Properties();
      InputStream in = Main.class.getResourceAsStream(propertiesFile);
      prop.load(in);
      in.close();
      return prop.getProperty("version");
    } catch (IOException e) {
      throw new AssertionError("Could not load oksocial-version.properties.");
    }
  }

  public static boolean isOSX() {
    String osName = System.getProperty("os.name");
    return osName.contains("OS X");
  }

  public static boolean isLinux() {
    String osName = System.getProperty("os.name");
    return osName.contains("Linux");
  }

  public static <T> Optional<T> or(Optional<T> option, Supplier<Optional<T>> callable) {
    if (option.isPresent()) {
      return option;
    } else {
      return callable.get();
    }
  }

  public static <T> CompletableFuture<T> failedFuture(Exception e) {
    CompletableFuture<T> f = new CompletableFuture<>();
    f.completeExceptionally(e);
    return f;
  }

  public static <T> Stream<T> optionalStream(Optional<T> ai) {
    return ai.isPresent() ? Stream.of(ai.get()) : Stream.empty();
  }
}
