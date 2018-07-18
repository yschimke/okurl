package com.baulsupp.okurl.services.stackexchange.model

data class Question(val tags: List<String>, val title: String, val answer_count: Int, val link: String, val creation_date: Long)

data class Questions(val items: List<Question>, val has_more: Boolean, val quota_max: Int, val quota_remaining: Int)

data class MeResponse(val items: List<User>, val quota_max: Int, val quota_remaining: Int, val has_more: Boolean)

data class User(
  val reputation_change_quarter: Int,
  val link: String,
  val last_modified_date: Int,
  val last_access_date: Int,
  val reputation: Int,
  val badge_counts: BadgeCounts,
  val creation_date: Int,
  val display_name: String,
  val reputation_change_year: Int,
  val accept_rate: Int,
  val is_employee: Boolean,
  val profile_image: String,
  val account_id: Int,
  val user_type: String,
  val website_url: String,
  val reputation_change_week: Int,
  val user_id: Int,
  val reputation_change_day: Int,
  val location: String,
  val age: Int?,
  val reputation_change_month: Int
)

data class BadgeCounts(val gold: Int = 0, val silver: Int = 0, val bronze: Int = 0)
