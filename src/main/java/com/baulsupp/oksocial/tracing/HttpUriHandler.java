package com.baulsupp.oksocial.tracing;

import java.net.URI;
import java.util.Optional;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class HttpUriHandler implements UriHandler {
  @Override public Optional<Sender> buildSender(URI uri) {
    if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
      return of(OkHttpSender.create(uri.toString()));
    }
    return empty();
  }
}
