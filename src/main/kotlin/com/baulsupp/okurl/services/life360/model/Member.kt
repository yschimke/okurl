package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Member(
  val activity: Any?,
  val avatar: String,
  val communications: List<Communication>,
  val createdAt: String,
  val features: MemberFeatures,
  val firstName: String,
  val id: String,
  val isAdmin: String,
  val issues: Issues,
  val lastName: String,
  val location: Location,
  val loginEmail: String,
  val loginPhone: String,
  val medical: Any?,
  val pinNumber: Any?,
  val relation: Any?
)
