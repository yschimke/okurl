package com.baulsupp.okurl.services.quip.model

data class User(
  val name: String,
  val id: String,
  val desktop_folder_id: String?,
  val archive_folder_id: String?,
  val starred_folder_id: String?,
  val private_folder_id: String?,
  val shared_folder_ids: List<String>?,
  val emails: List<String>?,
  val profile_picture_url: String?,
  val affinity: Double
)
