package com.baulsupp.okurl.services.github.model


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
  val avatar_url: String,
  val bio: String? = null,
  val blog: String? = null,
  val collaborators: Int? = null,
  val company: String? = null,
  val created_at: String? = null,
  val disk_usage: Int? = null,
  val email: String? = null,
  val events_url: String? = null,
  val followers: Int? = null,
  val followers_url: String? = null,
  val following: Int? = null,
  val following_url: String? = null,
  val gists_url: String? = null,
  val gravatar_id: String? = null,
  val hireable: Any? = null,
  val html_url: String,
  val id: Int,
  val location: String? = null,
  val login: String,
  val name: String? = null,
  val node_id: String? = null,
  val organizations_url: String? = null,
  val owned_private_repos: Int? = null,
  val plan: Plan? = null,
  val private_gists: Int? = null,
  val public_gists: Int? = null,
  val public_repos: Int? = null,
  val received_events_url: String? = null,
  val repos_url: String? = null,
  val site_admin: Boolean? = null,
  val starred_url: String? = null,
  val subscriptions_url: String? = null,
  val total_private_repos: Int? = null,
  val twitter_username: Any? = null,
  val two_factor_authentication: Boolean? = null,
  val type: String,
  val updated_at: String? = null,
  val url: String
)
