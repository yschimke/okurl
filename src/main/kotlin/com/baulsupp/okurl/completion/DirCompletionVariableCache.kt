package com.baulsupp.okurl.completion

import java.io.File
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class DirCompletionVariableCache(val dir: File = File(System.getProperty("java.io.tmpdir"))) : CompletionVariableCache {
  override fun get(service: String, key: String): List<String>? {
    val f = File(dir, "$service-$key.txt")

    // cache for 1 minutes
    if (f.isFile && f.lastModified() > System.currentTimeMillis() - 60000) {
      try {
        return f.readLines().filterNot { it.isBlank() }
      } catch (e: IOException) {
        logger.log(Level.WARNING, "failed to read variables", e)
      }
    }

    return null
  }

  override fun set(service: String, key: String, values: List<String>) {
    val f = File(dir, "$service-$key.txt")

    try {
      f.writeText(values.joinToString(separator = "\n", postfix = "\n"))
    } catch (e: IOException) {
      logger.log(Level.WARNING, "failed to store variables", e)
    }
  }

  companion object {
    private val logger = Logger.getLogger(DirCompletionVariableCache::class.java.name)

    val TEMP = DirCompletionVariableCache()
  }
}
