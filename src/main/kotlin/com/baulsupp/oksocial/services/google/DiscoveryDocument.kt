package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.output.util.JsonUtil
import java.io.IOException
import java.util.Collections
import java.util.Comparator
import java.util.Optional
import java.util.stream.Stream

import java.util.stream.Collectors.toList

/**
 * https://developers.google.com/discovery/v1/using
 */
class DiscoveryDocument(private val map: Map<String, Any>) {

    val baseUrl: String
        get() = "" + map["rootUrl"] + map["servicePath"]

    val urls: List<String>
        get() = endpoints
                .stream()
                .map { e -> e.url() }
                .distinct()
                .collect<List<String>, Any>(toList())

    val endpoints: List<DiscoveryEndpoint>
        get() = expandEndpoints(map)

    private fun expandEndpoints(map: Map<String, Any>): List<DiscoveryEndpoint> {
        val resources = getResources(map)

        return resources.values
                .stream()
                .flatMap { r ->
                    Stream.concat(
                            getMethods(r).values.stream().map { m -> DiscoveryEndpoint(baseUrl, m) },
                            expandEndpoints(r).stream())
                }
                .collect<List<DiscoveryEndpoint>, Any>(toList())
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

    fun findEndpoint(url: String): Optional<DiscoveryEndpoint> {
        val endpointStream = endpoints.stream().filter { e -> matches(url, e) }

        return endpointStream.sorted(methodFirst("GET")).findFirst()
    }

    private fun methodFirst(method: String): Comparator<in DiscoveryEndpoint> {
        return { o1, o2 ->
            val m1 = o1.httpMethod()
            val m2 = o2.httpMethod()

            if (m1 == method && m2 != method) {
                return -1
            } else if (m2 == method) {
                return 1
            }

            0
        } as Comparator<DiscoveryEndpoint>
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