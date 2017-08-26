package com.baulsupp.oksocial.tracing;

import java.net.URI;
import java.util.Optional;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.okhttp3.OkHttpSender;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class HttpUriHandler implements UriHandler {
  @Override public Optional<Reporter<Span>> buildSender(URI uri) {
    if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
      OkHttpSender sender = OkHttpSender.create(uri.toString());
      AsyncReporter<Span> reporter = AsyncReporter.create(sender);

      return of(reporter);
    }
    return empty();
  }
}
