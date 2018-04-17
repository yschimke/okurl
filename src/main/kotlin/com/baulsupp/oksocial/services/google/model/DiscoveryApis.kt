package com.baulsupp.oksocial.services.google.model

data class DiscoveryApis(
  val kind: String = "",
  val items: List<ItemsItem>?,
  val discoveryVersion: String = ""
)

data class ItemsItem(
  val discoveryRestUrl: String = "",
  val kind: String = "",
  val name: String = "",
  val description: String = "",
  val documentationLink: String = "",
  val id: String = "",
  val title: String = "",
  val icons: Any,
  val version: String = "",
  val preferred: Boolean = false
)

data class DiscoveryIndexMap(val index: Map<String, List<String>>)
