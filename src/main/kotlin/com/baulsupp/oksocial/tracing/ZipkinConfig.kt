package com.baulsupp.oksocial.tracing

import brave.propagation.TraceContext
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Properties

class ZipkinConfig(private val senderUri: String?, private val displayUrl: String?) {

    fun zipkinSenderUri(): String? {
        return senderUri
    }

    fun openFunction(): (TraceContext) -> String? {
        return { traceContext -> displayUrl?.replace(Regex("""\{traceid\}"""), traceContext.traceIdString()) }
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
