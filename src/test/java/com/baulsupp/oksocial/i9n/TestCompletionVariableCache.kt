package com.baulsupp.oksocial.i9n

import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.google.common.collect.Maps
import java.util.Optional

class TestCompletionVariableCache : CompletionVariableCache {
  private val cache = Maps.newConcurrentMap<String, List<String>>()

  override fun get(service: String, key: String): Optional<List<String>> {
    return Optional.ofNullable(cache[service + "-" + key])
  }

  override fun store(service: String, key: String, values: List<String>) {
    cache.put(service + "-" + key, values)
  }
}
