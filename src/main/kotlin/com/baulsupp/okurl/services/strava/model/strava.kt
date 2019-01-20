package com.baulsupp.okurl.services.strava.model

data class Athlete(
  val country: Any? = null,
  val profile_medium: String? = null,
  val firstname: String,
  val follower: Any? = null,
  val city: Any? = null,
  val resource_state: Int?,
  val sex: String?,
  val profile: String? = null,
  val created_at: String?,
  val summit: Boolean?,
  val lastname: String,
  val premium: Boolean?,
  val updated_at: String,
  val badge_type_id: Int,
  val friend: Any? = null,
  val id: Int,
  val state: Any? = null,
  val email: String?,
  val username: String
)

data class ActivitySummary(
  val utc_offset: Int,
  val comment_count: Int,
  val upload_id: Long,
  val private: Boolean,
  val distance: Double,
  val timezone: String,
  val location_country: Any? = null,
  val has_kudoed: Boolean,
  val has_heartrate: Boolean,
  val external_id: String,
  val type: String,
  val location_state: Any? = null,
  val manual: Boolean,
  val gear_id: Any? = null,
  val elev_low: Double,
  val flagged: Boolean,
  val end_latlng: List<Double>?,
  val start_latitude: Double,
  val trainer: Boolean,
  val location_city: Any? = null,
  val total_photo_count: Int,
  val elapsed_time: Int,
  val heartrate_opt_out: Boolean,
  val display_hide_heartrate_option: Boolean,
  val id: Int,
  val kudos_count: Int,
  val map: Map,
  val average_speed: Double,
  val moving_time: Int,
  val start_date: String,
  val pr_count: Int,
  val visibility: String,
  val athlete: AthleteLink,
  val athlete_count: Int,
  val resource_state: Int,
  val start_date_local: String,
  val max_speed: Double,
  val total_elevation_gain: Double,
  val from_accepted_tag: Boolean,
  val start_latlng: List<Double>?,
  val start_longitude: Double,
  val photo_count: Int,
  val elev_high: Double,
  val achievement_count: Int,
  val suffer_score: Any? = null,
  val name: String,
  val commute: Boolean?
)

data class AthleteLink(
  val resource_state: Int,
  val id: Int
)

data class Map(
  val polyline: String? = null,
  val summary_polyline: String,
  val resource_state: Int,
  val id: String
)
