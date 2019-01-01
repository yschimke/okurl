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
