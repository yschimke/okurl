package com.baulsupp.oksocial.tracing;

import java.net.URI;
import java.util.Optional;
import java.util.ServiceLoader;
import zipkin.reporter.Sender;

public interface UriHandler {
  static ServiceLoader<UriHandler> loadServices() {
    return ServiceLoader.load(UriHandler.class);
  }

  Optional<Sender> buildSender(URI uri);
}