package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.output.util.JsonUtil
import com.google.common.io.Resources
import java.io.IOException
import java.nio.charset.StandardCharsets

/*
 * API URL -> Discovery URL
 */
class DiscoveryIndex(private val map: Map<String, List<String>>) {

  /*
   * Exact search
   */
  fun getDiscoveryUrlForApi(api: String): List<String> = map[api] ?: listOf()

  /*
   * Prefix search (returns longest)
   */
  fun getDiscoveryUrlForPrefix(prefix: String): List<String> = map.entries
          .filter { s1 -> match(prefix, s1.key) }
          .flatMap { s -> s.value }

  internal fun match(prefix: String, indexKey: String): Boolean =
          indexKey.startsWith(prefix) || prefix.startsWith(indexKey)

  companion object {

    @Throws(IOException::class)
    fun loadStatic(): DiscoveryIndex {
      val url = DiscoveryIndex::class.java.getResource("index.json")

      val definition = Resources.toString(url, StandardCharsets.UTF_8)

      return DiscoveryIndex.parse(definition)
    }

    @Throws(IOException::class)
    fun parse(definition: String): DiscoveryIndex {
      val m = JsonUtil.map(definition)
      return DiscoveryIndex(m as Map<String, List<String>>)
    }
  }
}
