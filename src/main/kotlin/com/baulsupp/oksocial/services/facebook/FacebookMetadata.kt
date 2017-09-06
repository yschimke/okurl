package com.baulsupp.oksocial.services.facebook

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets

import java.util.stream.Collectors.toList

class FacebookMetadata(metadata: Map<String, Any>?) {
    private val metadata: Map<String, Any>

    init {
        this.metadata = metadata ?: Maps.newHashMap()
    }

    fun connections(): Set<String> {
        val connections = metadata["connections"] as Map<String, Any>
        return connections?.keys ?: Sets.newHashSet()
    }

    fun fields(): List<Map<String, String>> {
        val fields = metadata["fields"] as List<Map<String, String>>
        return fields ?: Lists.newArrayList()
    }

    fun fieldNames(): List<String> {
        return fields().stream().map<String> { f -> f["name"] }.collect<List<String>, Any>(toList())
    }
}
