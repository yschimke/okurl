package com.baulsupp.okurl.services.dropbox.model

import com.squareup.moshi.Json

data class DropboxFileList(val entries: List<DropboxFile>, val cursor: String, val has_more: Boolean)

data class DropboxFile(
  @Json(name = ".tag") val tag: String,
  val name: String,
  val path_display: String,
  val id: String,
  val rev: String?,
  val size: Int?,
  val content_hash: String?
)
