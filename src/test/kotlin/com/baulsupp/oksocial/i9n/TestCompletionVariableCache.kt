package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.completion.CompletionVariableCache
import java.util.concurrent.ConcurrentHashMap

class TestCompletionVariableCache : CompletionVariableCache {
  private val cache = ConcurrentHashMap<String, List<String>>()

  override fun get(service: String, key: String): List<String>? {
    return cache["$service-$key"]
  }

  override fun set(service: String, key: String, values: List<String>) {
    cache["$service-$key"] = values
  }
}
