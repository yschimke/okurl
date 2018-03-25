package com.baulsupp.oksocial.tracing

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.tracing.UriHandler.Companion.loadServices
import zipkin2.Span
import zipkin2.reporter.Reporter
import java.net.URI
import java.util.ArrayList
import java.util.ServiceLoader

/**
 * Registry for looking up transports by URI.
 *
 *
 * Uses the Jar Services mechanism with services defined by [UriHandler].
 */
class UriTransportRegistry(services: ServiceLoader<UriHandler>) {
  private val handlers: List<UriHandler>

  init {
    handlers = ArrayList()
    services.forEach { handlers.add(it) }
  }

  private fun findClient(uriString: String): Reporter<Span> {
    val uri = URI.create(uriString)

    return handlers.mapNotNull { it.buildSender(uri) }.firstOrNull()
      ?: throw UsageException("unknown zipkin sender: $uriString")
  }

  companion object {

    fun fromServices(): UriTransportRegistry {
      val services = loadServices()

      return UriTransportRegistry(services)
    }

    fun forUri(uri: String): Reporter<Span> {
      return UriTransportRegistry.fromServices().findClient(uri)
    }
  }
}
