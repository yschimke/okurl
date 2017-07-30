package com.baulsupp.oksocial.tracing;

import brave.propagation.TraceContext;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

public class ZipkinConfig {
  private String senderUri;
  private String displayUrl;
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
        String sender = p.getProperty("SENDER", "http://localhost:9411/api/v1/spans");
        String display = p.getProperty("DISPLAY", "http://localhost:9411/zipkin/traces/{traceid}");
        return new ZipkinConfig(sender, display);
      }
    } else {
      return unconfigured();
    }
  }

  public static ZipkinConfig unconfigured() {
    return new ZipkinConfig("http://localhost:9411/api/v1/spans",
        "http://localhost:9411/zipkin/traces/{traceid}");
  }

  public String zipkinSenderUri() {
    return senderUri;
  }

  public Function<TraceContext, String> openFunction() {
    return traceContext -> displayUrl.replaceAll("\\{traceid\\}", traceContext.traceIdString());
  }
}
