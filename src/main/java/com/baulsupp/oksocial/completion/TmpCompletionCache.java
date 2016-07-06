package com.baulsupp.oksocial.completion;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class TmpCompletionCache implements CompletionCache {
  private final File dir;

  public TmpCompletionCache() {
    this.dir = new File(System.getProperty("java.io.tmpdir"));
  }

  @Override public Optional<List<String>> get(String service, String key, boolean freshOnly)
      throws IOException {
    File f = new File(dir, service + "-" + key + ".txt");

    // cache for 5 minutes
    if (f.isFile() && (!freshOnly || f.lastModified() > System.currentTimeMillis() - 300000)) {
      return Optional.of(Files.readLines(f, StandardCharsets.UTF_8));
    } else {
      return Optional.empty();
    }
  }

  @Override public void store(String service, String key, List<String> values) throws IOException {
    File f = new File(dir, service + "-" + key + ".txt");

    Files.write(values.stream().collect(joining("\n")), f, StandardCharsets.UTF_8);
  }
}
