package com.baulsupp.okurl.services.giphy.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Pagination(val total_count: Int, val count: Int, val offset: Int)

@JsonClass(generateAdapter = true)
data class Meta(val status: Int, val msg: String, val response_id: String)

@JsonClass(generateAdapter = true)
data class Image(
  val url: String?,
  val width: String?,
  val height: String?,
  val size: String?,
  val mp4: String?,
  val mp4_size: String?,
  val webp: String?,
  val webp_size: String?
)

@JsonClass(generateAdapter = true)
data class ImageResult(val type: String, val id: String, val url: String, val images: Map<String, Image>)

@JsonClass(generateAdapter = true)
data class SearchResults(val data: List<ImageResult>, val pagination: Pagination, val meta: Meta)
