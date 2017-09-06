package com.baulsupp.oksocial.services.google

import com.google.common.collect.Lists
import java.util.regex.Pattern

import java.util.stream.Collectors.toList

class DiscoveryEndpoint(private val baseUrl: String, private val map: Map<String, Any>) {

    fun id(): String {
        return getRequired("id") as String
    }

    fun url(): String {
        return baseUrl + path()
    }

    private fun path(): String {
        return getRequired("path") as String
    }

    private fun getRequired(name: String): Any {
        if (!map.containsKey(name)) {
            throw NullPointerException("path not found")
        }

        return map[name]
    }

    fun description(): String {
        return getRequired("description") as String
    }

    fun scopeNames(): List<String> {
        val scopes = map["scopes"] as List<String>

        return scopes ?: Lists.newArrayList()

    }

    fun parameters(): List<DiscoveryParameter> {
        val o = map["parameters"] as Map<String, Map<String, Any>> ?: return Lists.newArrayList()

        return o.entries
                .stream()
                .map { p -> DiscoveryParameter(p.key, p.value) }
                .collect<List<DiscoveryParameter>, Any>(toList())
    }

    fun matches(requestUrl: String): Boolean {
        if (!requestUrl.startsWith(baseUrl)) {
            return false
        }

        val requestUrlPath = requestUrl.substring(baseUrl.length)

        return buildDocPathRegex().matcher(requestUrlPath).matches()
    }

    private fun buildDocPathRegex(): Pattern {
        val parameters = parameters()

        var hasQueryParams = false

        var pathPattern = this.path()

        for (p in parameters) {
            if (p.location() == "path") {
                var pPattern: String? = p.pattern()
                if (pPattern == null) {
                    pPattern = ".*"
                } else if (pPattern.matches("\\^.*\\$".toRegex())) {
                    pPattern = pPattern.substring(1, pPattern.length - 1)
                }
                val x = "\\{\\+?" + p.name() + "\\}"
                pathPattern = pathPattern.replace(x.toRegex(), pPattern)
            } else if (p.location() == "query") {
                hasQueryParams = true
            }
        }

        return Pattern.compile(pathPattern + if (hasQueryParams) "(\\?.*)?" else "")
    }

    fun httpMethod(): String {
        return (map as java.util.Map<String, Any>).getOrDefault("httpMethod", "GET") as String
    }
}
