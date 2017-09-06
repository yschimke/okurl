package com.baulsupp.oksocial.services.google

import com.google.common.collect.Lists
import com.google.common.io.Resources
import com.baulsupp.oksocial.output.util.JsonUtil
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Optional

import java.util.stream.Collectors.toList

/*
 * API URL -> Discovery URL
 */
class DiscoveryIndex(private val map: Map<String, List<String>>) {

    /*
   * Exact search
   */
    fun getDiscoveryUrlForApi(api: String): List<String> {
        return Optional.ofNullable(map[api]).orElse(Lists.newArrayList())
    }

    /*
   * Prefix search (returns longest)
   */
    fun getDiscoveryUrlForPrefix(prefix: String): List<String> {
        return map.entries
                .stream()
                .filter { s1 -> match(prefix, s1.key) }
                .flatMap { s -> s.value.stream() }
                .collect<List<String>, Any>(toList())
    }

    internal fun match(prefix: String, indexKey: String): Boolean {
        return indexKey.startsWith(prefix) || prefix.startsWith(indexKey)
    }

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
            return DiscoveryIndex(m)
        }
    }
}
