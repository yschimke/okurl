package com.baulsupp.oksocial.tracing;

import java.net.URI;
import java.util.Optional;
import java.util.ServiceLoader;
import zipkin.Span;
import zipkin.reporter.Reporter;

public interface UriHandler {
  static ServiceLoader<UriHandler> loadServices() {
    return ServiceLoader.load(UriHandler.class);
  }

  Optional<Reporter<Span>> buildSender(URI uri);
}