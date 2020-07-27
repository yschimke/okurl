package com.baulsupp.okurl.services.strava.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
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
  val id: Long,
  val state: Any? = null,
  val email: String?,
  val username: String
)

@JsonClass(generateAdapter = true)
data class Lap(
  val activity: ActivityLink? = null,
  val athlete: AthleteLink? = null,
  val average_heartrate: Double? = null,
  val average_speed: Double? = null,
  val average_watts: Double? = null,
  val device_watts: Boolean? = null,
  val distance: Double? = null,
  val elapsed_time: Int? = null,
  val end_index: Int? = null,
  val id: Long? = null,
  val lap_index: Int? = null,
  val max_heartrate: Int? = null,
  val max_speed: Double? = null,
  val moving_time: Int? = null,
  val name: String? = null,
  val resource_state: Int? = null,
  val split: Int? = null,
  val start_date: String? = null,
  val start_date_local: String? = null,
  val start_index: Int? = null,
  val total_elevation_gain: Double? = null
)

@JsonClass(generateAdapter = true)
data class ActivitySummary(
  val achievement_count: Int? = null,
  val athlete: AthleteLink? = null,
  val athlete_count: Int? = null,
  val available_zones: List<String?>? = null,
  val average_heartrate: Double? = null,
  val average_speed: Double? = null,
  val average_watts: Double? = null,
  val calories: Double? = null,
  val comment_count: Int? = null,
  val commute: Boolean? = null,
  val description: Any? = null,
  val device_name: String? = null,
  val device_watts: Boolean? = null,
  val display_hide_heartrate_option: Boolean? = null,
  val distance: Double,
  val elapsed_time: Int,
  val elev_high: Double? = null,
  val elev_low: Double? = null,
  val embed_token: String? = null,
  val end_latlng: List<Double?>? = null,
  val external_id: Any? = null,
  val flagged: Boolean? = null,
  val from_accepted_tag: Boolean? = null,
  val gear_id: Any? = null,
  val has_heartrate: Boolean? = null,
  val has_kudoed: Boolean? = null,
  val heartrate_opt_out: Boolean? = null,
  val id: Long? = null,
  val kilojoules: Double? = null,
  val kudos_count: Int? = null,
  val laps: List<Lap?>? = null,
  val location_city: Any? = null,
  val location_country: Any? = null,
  val location_state: Any? = null,
  val manual: Boolean? = null,
  val map: ActivityMap? = null,
  val max_heartrate: Int? = null,
  val max_speed: Double? = null,
  val moving_time: Int? = null,
  val name: String? = null,
  val perceived_exertion: Any? = null,
  val photo_count: Int? = null,
  val photos: Any? = null,
  val pr_count: Int? = null,
  val prefer_perceived_exertion: Any? = null,
  val `private`: Boolean? = null,
  val resource_state: Int? = null,
  val segment_efforts: List<SegmentEffort?>? = null,
  val splits_metric: List<Splits>? = null,
  val splits_standard: List<Splits>? = null,
  val start_date: String? = null,
  val start_date_local: String? = null,
  val start_latitude: Double? = null,
  val start_latlng: List<Double?>? = null,
  val start_longitude: Double? = null,
  val suffer_score: Int? = null,
  val timezone: String? = null,
  val total_elevation_gain: Double? = null,
  val total_photo_count: Int? = null,
  val trainer: Boolean? = null,
  val type: String? = null,
  val upload_id: Long? = null,
  val upload_id_str: String? = null,
  val utc_offset: Int? = null,
  val visibility: String? = null,
  val workout_type: Any? = null
)

@JsonClass(generateAdapter = true)
data class ActivityMap(
  val id: String? = null,
  val polyline: String? = null,
  val resource_state: Int? = null,
  val summary_polyline: String? = null
)

@JsonClass(generateAdapter = true)
data class SegmentEffort(
  val achievements: List<Any>? = null,
  val activity: ActivityLink? = null,
  val athlete: AthleteLink? = null,
  val average_heartrate: Double? = null,
  val average_watts: Double? = null,
  val device_watts: Boolean? = null,
  val distance: Double? = null,
  val elapsed_time: Int? = null,
  val end_index: Int? = null,
  val hidden: Boolean? = null,
  val id: Long? = null,
  val kom_rank: Any? = null,
  val max_heartrate: Int? = null,
  val moving_time: Int? = null,
  val name: String? = null,
  val pr_rank: Any? = null,
  val resource_state: Int? = null,
  val segment: Segment? = null,
  val start_date: String? = null,
  val start_date_local: String? = null,
  val start_index: Int? = null
)

@JsonClass(generateAdapter = true)
data class Segment(
  val activity_type: String? = null,
  val average_grade: Double? = null,
  val city: String? = null,
  val climb_category: Int? = null,
  val country: String? = null,
  val distance: Double? = null,
  val elevation_high: Double? = null,
  val elevation_low: Double? = null,
  val end_latitude: Double? = null,
  val end_latlng: List<Double?>? = null,
  val end_longitude: Double? = null,
  val hazardous: Boolean? = null,
  val id: Int? = null,
  val maximum_grade: Double? = null,
  val name: String? = null,
  val `private`: Boolean? = null,
  val resource_state: Int? = null,
  val starred: Boolean? = null,
  val start_latitude: Double? = null,
  val start_latlng: List<Double?>? = null,
  val start_longitude: Double? = null,
  val state: String? = null
)

@JsonClass(generateAdapter = true)
data class Splits(
  val average_grade_adjusted_speed: Any? = null,
  val average_heartrate: Double? = null,
  val average_speed: Double? = null,
  val distance: Double? = null,
  val elapsed_time: Int? = null,
  val elevation_difference: Double? = null,
  val moving_time: Int? = null,
  val pace_zone: Int? = null,
  val split: Int? = null
)

@JsonClass(generateAdapter = true)
data class ActivityLink(
  val id: Long? = null,
  val resource_state: Int? = null
)

@JsonClass(generateAdapter = true)
data class AthleteLink(
  val id: Int? = null,
  val resource_state: Int? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
  val token_type: String,
  val access_token: String,
  val athlete: Map<String, Any>?,
  val refresh_token: String,
  val expires_at: Long,
  val state: String? = null
)
