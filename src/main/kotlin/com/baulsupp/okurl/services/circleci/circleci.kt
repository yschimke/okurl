package com.baulsupp.okurl.services.circleci

data class User(
  val selected_email: String,
  val name: String,
  val github_id: Int?,
  val identities: Map<String, Map<String, Any>>,
  val login: String
)

data class Project(
  val vcs_url: String,
  val language: String?,
  val vcs_type: String,
  val username: String,
  val reponame: String
)

data class BuildUser(
  val avatarUrl: String? = null,
  val id: Int? = null,
  val isUser: Boolean? = null,
  val login: String? = null,
  val name: String? = null,
  val vcsType: String? = null
)

data class Build(
  val author_date: String? = null,
  val author_email: String? = null,
  val author_name: String? = null,
  val body: String? = null,
  val branch: String? = null,
  val build_num: Int = 0,
  val build_time_millis: Int? = null,
  val build_url: String? = null,
  val committer_date: String? = null,
  val committer_email: String? = null,
  val committer_name: String? = null,
  val dont_build: Any? = null,
  val fleet: String? = null,
  val lifecycle: String? = null,
  val outcome: String? = null,
  val parallel: Int? = null,
  val platform: String? = null,
  val pull_requests: List<Any>? = null,
  val queuedAt: String? = null,
  val reponame: String? = null,
  val start_time: String? = null,
  val status: String? = null,
  val stop_time: String? = null,
  val subject: String? = null,
  val usage_queued_at: String? = null,
  val user: BuildUser,
  val username: String? = null,
  val vcs_revision: String? = null,
  val vcs_tag: Any? = null,
  val vcs_url: String? = null,
  val why: String? = null,
  val workflows: Workflows? = null
)

data class Workflows(
  val job_name: String? = null,
  val workflow_name: String? = null
)

data class Test(
  val classname: String? = null,
  val `file`: Any? = null,
  val message: Any? = null,
  val name: String? = null,
  val result: String? = null,
  val run_time: Double? = null,
  val source: String? = null,
  val source_type: String? = null
)

data class TestMetaData(
  val exceptions: List<Any>? = null,
  val tests: List<Test>) {

  val failedTests: List<Test>
    get() = tests.filterNot { it.result == "success" || it.result == "skipped" }
}

