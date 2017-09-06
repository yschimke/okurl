package com.baulsupp.oksocial.tracing

import brave.propagation.TraceContext
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Optional
import java.util.Properties
import java.util.function.Function

import java.util.Optional.ofNullable

class ZipkinConfig(private val senderUri: String?, private val displayUrl: String?) {

    fun zipkinSenderUri(): Optional<String> {
        return ofNullable(senderUri)
    }

    fun openFunction(): Function<TraceContext, Optional<String>> {
        return { traceContext -> ofNullable(displayUrl).map<String> { url -> url.replace("\\{traceid\\}".toRegex(), traceContext.traceIdString()) } }
    }

    companion object {
        private val zipkinRc = File(System.getenv("HOME"), ".zipkinrc")

        @Throws(IOException::class)
        fun load(): ZipkinConfig {
            if (zipkinRc.exists()) {
                FileReader(zipkinRc).use { r ->
                    val p = Properties()
                    p.load(r)
                    val sender = p.getProperty("SENDER")
                    val display = p.getProperty("DISPLAY")
                    return ZipkinConfig(sender, display)
                }
            } else {
                return unconfigured()
            }
        }

        fun unconfigured(): ZipkinConfig {
            return ZipkinConfig(null, null)
        }
    }
}
