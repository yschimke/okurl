package com.baulsupp.oksocial.tracing

import zipkin.Span
import zipkin.reporter.AsyncReporter
import zipkin.reporter.Reporter
import zipkin.reporter.okhttp3.OkHttpSender
import java.net.URI

class HttpUriHandler : UriHandler {
  override fun buildSender(uri: URI): Reporter<Span>? = when {
    uri.scheme == "http" || uri.scheme == "https" -> AsyncReporter.create(OkHttpSender.create(uri.toString()))
    else -> null
  }
}
