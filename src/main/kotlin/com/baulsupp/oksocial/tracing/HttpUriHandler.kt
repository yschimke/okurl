package com.baulsupp.oksocial.tracing

import java.net.URI
import java.util.Optional
import zipkin.Span
import zipkin.reporter.AsyncReporter
import zipkin.reporter.Reporter
import zipkin.reporter.okhttp3.OkHttpSender

import java.util.Optional.empty
import java.util.Optional.of

class HttpUriHandler : UriHandler {
    override fun buildSender(uri: URI): Optional<Reporter<Span>> {
        if (uri.scheme == "http" || uri.scheme == "https") {
            val sender = OkHttpSender.create(uri.toString())
            val reporter = AsyncReporter.create(sender)

            return of(reporter)
        }
        return empty()
    }
}
