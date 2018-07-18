package com.baulsupp.okurl.tracing

import zipkin2.Span
import zipkin2.reporter.Reporter
import java.net.URI
import java.util.ServiceLoader

interface UriHandler {
  fun buildSender(uri: URI): Reporter<Span>?

  companion object {
    fun loadServices(): ServiceLoader<UriHandler> {
      return ServiceLoader.load(UriHandler::class.java)
    }
  }
}
