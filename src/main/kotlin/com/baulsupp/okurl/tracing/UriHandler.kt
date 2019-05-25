package com.baulsupp.okurl.tracing

import zipkin2.Span
import zipkin2.reporter.Reporter
import java.net.URI

interface UriHandler {
  fun buildSender(uri: URI): Reporter<Span>?

  companion object {
    fun loadServices(): List<UriHandler> {
      return listOf(HttpUriHandler())
    }
  }
}
