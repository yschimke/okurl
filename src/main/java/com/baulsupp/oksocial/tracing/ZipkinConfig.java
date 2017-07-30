package com.baulsupp.oksocial.tracing;

import brave.propagation.TraceContext;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class ZipkinConfig {
  private @Nullable String senderUri;
  private @Nullable String displayUrl;
  private static File zipkinRc = new File(System.getenv("HOME"), ".zipkinrc");

  public ZipkinConfig(String senderUri, String displayUrl) {
    this.senderUri = senderUri;
    this.displayUrl = displayUrl;
  }

  public static ZipkinConfig load() throws IOException {
    if (zipkinRc.exists()) {
      try (FileReader r = new FileReader(zipkinRc)) {
        Properties p = new Properties();
        p.load(r);
        String sender = p.getProperty("SENDER");
        String display = p.getProperty("DISPLAY");
        return new ZipkinConfig(sender, display);
      }
    } else {
      return unconfigured();
    }
  }

  public static ZipkinConfig unconfigured() {
    return new ZipkinConfig(null, null);
  }

  public Optional<String> zipkinSenderUri() {
    return ofNullable(senderUri);
  }

  public Function<TraceContext, Optional<String>> openFunction() {
    return traceContext -> ofNullable(displayUrl).map(
        url -> url.replaceAll("\\{traceid\\}", traceContext.traceIdString()));
  }
}
