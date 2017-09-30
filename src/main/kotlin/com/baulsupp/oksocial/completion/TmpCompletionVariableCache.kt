package com.baulsupp.oksocial.completion

import com.google.common.io.Files
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.Logger

class TmpCompletionVariableCache : CompletionVariableCache {

    private val dir: File = File(System.getProperty("java.io.tmpdir"))

    override fun get(service: String, key: String): List<String>? {
        val f = File(dir, "$service-$key.txt")

        // cache for 5 minutes
        if (f.isFile && f.lastModified() > System.currentTimeMillis() - 300000) {
            try {
                return Files.readLines(f, StandardCharsets.UTF_8)
            } catch (e: IOException) {
                logger.log(Level.WARNING, "failed to read variables", e)
            }

        }

        return null
    }

    override fun store(service: String, key: String, values: List<String>) {
        val f = File(dir, "$service-$key.txt")

        try {
            Files.write(values.joinToString("\n"), f, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            logger.log(Level.WARNING, "failed to store variables", e)
        }

    }

    companion object {
        private val logger = Logger.getLogger(TmpCompletionVariableCache::class.java.name)
    }
}
