package com.baulsupp.oksocial.services.google

class DiscoveryParameter(private val name: String, private val map: Map<String, Any>) {

    fun name(): String {
        return name
    }

    fun type(): String {
        return map["type"] as String
    }

    fun description(): String {
        return map["description"] as String
    }

    fun location(): String {
        return map["location"] as String
    }

    fun pattern(): String {
        return map["pattern"] as String
    }
}
