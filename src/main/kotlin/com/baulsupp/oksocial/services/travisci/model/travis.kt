package com.baulsupp.oksocial.services.travisci.model

import com.squareup.moshi.Json
import java.time.Instant

data class Pagination(val offset: Int, val limit: Int, val count: Int, val is_first: Boolean, val is_last: Boolean)

data class User(val id: String, val login: String, val name: String, val github_id: String?, val avatar_url: String, val is_syncing: Boolean, val synced_at: String)

abstract class ResultList(@Json(name = "@pagination") open val pagination: Pagination)

data class Repository(val id: String, val name: String, val slug: String)

data class RepositoryList(val repositories: List<Repository>, override val pagination: Pagination) : ResultList(pagination)

data class BuildBranch(val name: String, @Json(name = "@href") val href: String)

data class BuildUser(val id: Int, val login: String?)

data class Commit(val ref: String?, val compare_url: String?, val committed_at: String?,
                  val id: Int, val message: String?, val sha: String)

data class BuildJob(val id: Int = 0,
                    @Json(name = "@href") val href: String) {
  val url: String get() = "https://api.travis-ci.org$href"
  val logUrl: String get() = "https://api.travis-ci.org$href/log"
  val logOutputTxt: String get() = "https://api.travis-ci.org$href/log.txt"
}

data class Permissions(val cancel: Boolean = false,
                       val read: Boolean = false,
                       val restart: Boolean = false)

data class Build(val private: Boolean?, val previous_state: String?, val finished_at: Instant, val jobs: List<BuildJob>,
                 val commit: Commit, val repository: Repository, val branch: BuildBranch?, val created_by: BuildUser?,
                 val duration: Int?, val number: String, val event_type: String, val pull_request_title: String?,
                 val updated_at: Instant?, val pull_request_number: Int?, @Json(name = "@permissions") val permissions: Permissions,
                 val started_at: Instant?, val id: Int, val state: String, val tag: Any?, @Json(name = "@href") val href: String) {
  val isErrored: Boolean get() = state == "errored" || state == "failed"

  val stateChar: String
    get() = when (state) {
      "passed" -> "✓"
      "errored" -> "❌"
      "failed" -> "❌"
      "cancelled" -> "-"
      else -> "?"
    }

  val url: String get() = "https://api.travis-ci.org$href"
}

data class BuildList(val builds: List<Build>, @Json(name = "@pagination") override val pagination: Pagination) : ResultList(pagination)
