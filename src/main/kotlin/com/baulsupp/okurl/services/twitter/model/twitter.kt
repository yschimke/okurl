package com.baulsupp.okurl.services.twitter.model

data class User(
  val id_str: String,
  val name: String,
  val screen_name: String,
  val location: String,
  val description: String
)

data class Media(
  val id_str: String,
  val media_url_https: String,
  val display_url: String,
  val expanded_url: String,
  val type: String,
  val sizes: Map<String, Any> = mapOf()
)

data class Entities(
  val hashtags: List<Any> = listOf(),
  val symbols: List<Any> = listOf(),
  val user_mentions: List<Any> = listOf(),
  val urls: List<Any> = listOf(),
  val media: List<Media> = listOf()
)

data class Tweet(val id_str: String, val full_text: String, val user: User, val entities: Entities? = null)

data class SearchResults(val statuses: List<Tweet>)
