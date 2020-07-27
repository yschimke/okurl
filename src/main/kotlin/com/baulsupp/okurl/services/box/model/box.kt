package com.baulsupp.okurl.services.box.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Entry(val id: String, val type: String, val sequence_id: String, val etag: String, val name: String)

@JsonClass(generateAdapter = true)
data class Order(val by: String, val direction: String)

@JsonClass(generateAdapter = true)
data class FolderItems(
  val total_count: Int,
  val entries: List<Entry>,
  val offset: Int,
  val limit: Int,
  val order: List<Order>
)

@JsonClass(generateAdapter = true)
data class User(val id: String, val name: String, val email: String)
