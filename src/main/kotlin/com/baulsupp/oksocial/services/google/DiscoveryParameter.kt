package com.baulsupp.oksocial.services.google

class DiscoveryParameter(private val name: String, private val map: Map<String, Any>) {
  fun name(): String = name

  fun type(): String? = map["type"] as String?

  fun description(): String? = map["description"] as String?

  fun location(): String? = map["location"] as String?

  fun pattern(): String? = map["pattern"] as String?
}
