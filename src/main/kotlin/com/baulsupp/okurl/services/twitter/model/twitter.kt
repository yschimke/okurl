package com.baulsupp.okurl.services.twitter.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
  val id_str: String,
  val name: String,
  val screen_name: String,
  val location: String,
  val description: String
)

@JsonClass(generateAdapter = true)
data class Media(
  val id_str: String,
  val media_url_https: String,
  val display_url: String,
  val expanded_url: String,
  val type: String,
  val sizes: Map<String, Any> = mapOf()
)

@JsonClass(generateAdapter = true)
data class Entities(
  val hashtags: List<Any> = listOf(),
  val symbols: List<Any> = listOf(),
  val user_mentions: List<Any> = listOf(),
  val urls: List<Any> = listOf(),
  val media: List<Media> = listOf()
)

@JsonClass(generateAdapter = true)
data class Tweet(val id_str: String, val full_text: String, val user: User, val entities: Entities? = null)

@JsonClass(generateAdapter = true)
data class SearchResults(val statuses: List<Tweet>)
