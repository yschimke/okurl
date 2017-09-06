package com.baulsupp.oksocial.tracing

import java.net.URI
import java.util.Optional
import java.util.ServiceLoader
import zipkin.Span
import zipkin.reporter.Reporter

interface UriHandler {

    fun buildSender(uri: URI): Optional<Reporter<Span>>

    companion object {
        fun loadServices(): ServiceLoader<UriHandler> {
            return ServiceLoader.load(UriHandler::class.java)
        }
    }
}