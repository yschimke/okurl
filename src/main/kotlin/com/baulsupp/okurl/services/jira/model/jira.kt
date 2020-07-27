package com.baulsupp.okurl.services.jira.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectCategory(val id: String, val name: String, val description: String, val self: String)

@JsonClass(generateAdapter = true)
data class Project(
  val id: String,
  val key: String,
  val name: String,
  val avatarUrls: Map<String, String>,
  val projectCategory: ProjectCategory?,
  val projectTypeKey: String
)
