package com.baulsupp.okurl.services.github.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Plan(
  val collaborators: Int? = null,
  val name: String? = null,
  val private_repos: Int? = null,
  val space: Int? = null
)
