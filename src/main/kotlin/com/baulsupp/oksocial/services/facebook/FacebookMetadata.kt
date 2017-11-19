package com.baulsupp.oksocial.services.facebook

import com.google.common.collect.Maps

class FacebookMetadata(metadata: Map<String, Any>?) {
  private val metadata: Map<String, Any> = metadata ?: Maps.newHashMap()

  fun connections(): Set<String> = (metadata["connections"] as Map<String, Any>).keys

  fun fields(): List<Map<String, String>> = metadata["fields"] as List<Map<String, String>>

  fun fieldNames(): List<String> = fields().map { it["name"]!! }
}
