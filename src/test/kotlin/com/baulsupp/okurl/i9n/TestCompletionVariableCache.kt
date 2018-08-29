package com.baulsupp.okurl.i9n

import com.baulsupp.okurl.completion.CompletionVariableCache
import java.util.concurrent.ConcurrentHashMap

class TestCompletionVariableCache : CompletionVariableCache {
  private val cache = ConcurrentHashMap<String, List<String>>()

  override operator fun get(service: String, key: String): List<String>? {
    return cache["$service-$key"]
  }

  override operator fun set(service: String, key: String, values: List<String>) {
    cache["$service-$key"] = values
  }
}
