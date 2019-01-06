package com.baulsupp.okurl.services.atlassian.model

data class AccessibleResource(
  val avatarUrl: String,
  val name: String,
  val id: String,
  val scopes: List<String>
)

data class ServerInfo(
  val baseUrl: String,
  val deploymentType: String,
  val scmInfo: String,
  val serverTime: String,
  val buildDate: String,
  val serverTitle: String,
  val version: String,
  val buildNumber: Int,
  val versionNumbers: List<Integer>?
)

data class Myself(
  val avatarUrls: Map<String, String>,
  val displayName: String,
  val active: Boolean,
  val timeZone: String,
  val groups: Map<String, Any>,
  val locale: String,
  val accountId: String,
  val emailAddress: String,
  val expand: String,
  val name: String,
  val self: String,
  val key: String,
  val applicationRoles: Map<String, Any>
)
