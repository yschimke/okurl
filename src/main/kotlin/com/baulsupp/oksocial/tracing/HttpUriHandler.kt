package com.baulsupp.oksocial.tracing

import zipkin2.Span
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.Reporter
import zipkin2.reporter.okhttp3.OkHttpSender
import java.net.URI

class HttpUriHandler : UriHandler {
  override fun buildSender(uri: URI): Reporter<Span>? = when {
    uri.scheme == "http" || uri.scheme == "https" -> AsyncReporter.create(OkHttpSender.create(uri.toString()))
    else -> null
  }
}
