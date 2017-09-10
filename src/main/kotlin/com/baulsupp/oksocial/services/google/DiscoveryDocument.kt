package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.output.util.JsonUtil
import java.io.IOException
import java.util.*
import java.util.stream.Collectors.toList
import java.util.stream.Stream

/**
 * https://developers.google.com/discovery/v1/using
 */
class DiscoveryDocument(private val map: Map<String, Any>) {

    val baseUrl: String
        get() = "" + map["rootUrl"] + map["servicePath"]

    val urls: List<String>
        get() = endpoints
                .map { e -> e.url() }
                .distinct()

    val endpoints: List<DiscoveryEndpoint>
        get() = expandEndpoints(map)

    private fun expandEndpoints(map: Map<String, Any>): List<DiscoveryEndpoint> {
        val resources = getResources(map)

        return resources.values.flatMap { r -> getMethods(r).values.map { m -> DiscoveryEndpoint(baseUrl, m) } + expandEndpoints(r) }
    }

    private fun getResources(map: Map<String, Any>): Map<String, Map<String, Any>> {
        return if (!map.containsKey("resources")) {
            emptyMap()
        } else map["resources"] as Map<String, Map<String, Any>>
    }

    private fun getMethods(resource: Map<String, Any>): Map<String, Map<String, Any>> {
        return if (!resource.containsKey("methods")) {
            emptyMap()
        } else resource["methods"] as Map<String, Map<String, Any>>
    }

    val apiName: String
        get() = map["title"] as String

    val docLink: String
        get() = map["documentationLink"] as String

    fun findEndpoint(url: String): DiscoveryEndpoint? {
        return endpoints.filter { e -> matches(url, e) }.sortedBy { it.httpMethod() != "GET" }.firstOrNull()
    }

    private fun matches(url: String, e: DiscoveryEndpoint): Boolean {
        return e.url() == url || e.matches(url)
    }

    companion object {
        @Throws(IOException::class)
        fun parse(definition: String): DiscoveryDocument {
            return DiscoveryDocument(JsonUtil.map(definition))
        }
    }
}