package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.services.google.model.DiscoveryIndexMap

/*
 * API URL -> Discovery URL
 */
class DiscoveryIndex(private val map: Map<String, List<String>>) {

  /*
   * Exact search
   */
  fun getDiscoveryUrlForApi(api: String): List<String> = map[api].orEmpty()

  /*
   * Prefix search (returns longest)
   */
  fun getDiscoveryUrlForPrefix(prefix: String): List<String> = map.entries
    .filter { s1 -> match(prefix, s1.key) }
    .flatMap { s -> s.value }

  internal fun match(prefix: String, indexKey: String): Boolean =
    indexKey.startsWith(prefix) || prefix.startsWith(indexKey)

  companion object {
    val instance by lazy {
      parse(DiscoveryIndex::class.java.getResource("index.json")!!.readText())
    }

    fun parse(definition: String): DiscoveryIndex {
      return DiscoveryIndex(
        Main.moshi.adapter(DiscoveryIndexMap::class.java).fromJson(definition)!!.index)
    }
  }
}
