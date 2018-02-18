package com.baulsupp.oksocial.services.travisci.model

import com.squareup.moshi.Json

data class Pagination(val offset: Int, val limit: Int, val count: Int, val is_first: Boolean, val is_last: Boolean)

data class User(val id: String, val login: String, val name: String, val github_id: String?, val avatar_url: String, val is_syncing: Boolean, val synced_at: String)

abstract class ResultList(@Json(name = "@pagination") open val pagination: Pagination)

data class Repository(val id: String, val name: String, val slug: String)

data class RepositoryList(val repositories: List<Repository>, override val pagination: Pagination) : ResultList(pagination)

data class BuildBranch(val name: String, @Json(name = "@href") val href: String)

data class Build(val id: Int, val number: String, val state: String, val duration: Int?, val event_type: String, val branch: BuildBranch?) {
  val stateChar = when (state) {
    "passed" -> "✓"
    "errored" -> "❌"
    "cancelled" -> "-"
    else -> "?"
  }
}

data class BuildList(val builds: List<Build>, @Json(name = "@pagination") override val pagination: Pagination) : ResultList(pagination)
