package com.baulsupp.oksocial.completion;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.joining;

public class TmpCompletionVariableCache implements CompletionVariableCache {
  private static Logger logger = Logger.getLogger(TmpCompletionVariableCache.class.getName());

  private final File dir;

  public TmpCompletionVariableCache() {
    this.dir = new File(System.getProperty("java.io.tmpdir"));
  }

  @Override public Optional<List<String>> get(String service, String key) {
    File f = new File(dir, service + "-" + key + ".txt");

    // cache for 5 minutes
    if (f.isFile() && (f.lastModified() > System.currentTimeMillis() - 300000)) {
      try {
        return Optional.of(Files.readLines(f, StandardCharsets.UTF_8));
      } catch (IOException e) {
        logger.log(Level.WARNING, "failed to read variables", e);
      }
    }

    return Optional.empty();
  }

  @Override public void store(String service, String key, List<String> values) {
    File f = new File(dir, service + "-" + key + ".txt");

    try {
      Files.write(values.stream().collect(joining("\n")), f, StandardCharsets.UTF_8);
    } catch (IOException e) {
      logger.log(Level.WARNING, "failed to store variables", e);
    }
  }
}
