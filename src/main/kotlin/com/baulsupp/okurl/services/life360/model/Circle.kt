package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Circle(
  val color: String,
  val createdAt: String,
  val features: CircleFeatures,
  val id: String,
  val memberCount: String,
  val members: List<Member>?,
  val name: String,
  val type: String,
  val unreadMessages: String,
  val unreadNotifications: String
)
