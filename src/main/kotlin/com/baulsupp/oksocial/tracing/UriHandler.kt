package com.baulsupp.oksocial.tracing

import zipkin.Span
import zipkin.reporter.Reporter
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