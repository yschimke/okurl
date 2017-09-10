package com.baulsupp.oksocial.tracing

import com.baulsupp.oksocial.output.util.UsageException
import com.baulsupp.oksocial.tracing.UriHandler.Companion.loadServices
import zipkin.Span
import zipkin.reporter.Reporter
import java.net.URI
import java.util.*

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

        for (h in handlers) {
            val r = h.buildSender(uri)
            if (r.isPresent) {
                return r.get()
            }
        }

        throw UsageException("unknown zipkin sender: " + uriString)
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