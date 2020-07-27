package com.baulsupp.okurl.services.google.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscoveryApis(
  val kind: String,
  val items: List<ItemsItem>?,
  val discoveryVersion: String
)

@JsonClass(generateAdapter = true)
data class ItemsItem(
  val discoveryRestUrl: String,
  val kind: String,
  val name: String,
  val description: String,
  val documentationLink: String?,
  val id: String,
  val title: String,
  val icons: Any,
  val version: String,
  val preferred: Boolean
)

@JsonClass(generateAdapter = true)
data class DiscoveryIndexMap(val index: Map<String, List<String>>)

@JsonClass(generateAdapter = true)
data class DiscoveryDoc(
  val rootUrl: String,
  val servicePath: String,
  val resources: Map<String, Resource>?,
  val title: String,
  val documentationLink: String?,
  val baseUrl: String
)

@JsonClass(generateAdapter = true)
data class Resource(
  val resources: Map<String, Resource>?,
  val methods: Map<String, Method>?
)

@JsonClass(generateAdapter = true)
data class Method(
  val scopes: List<String>?,
  val parameters: Map<String, Parameter>?,
  val name: String?,
  val httpMethod: String?,
  val description: String?,
  val path: String,
  val id: String
)

@JsonClass(generateAdapter = true)
data class Parameter(val type: String, val location: String, val description: String?, val pattern: String?)
