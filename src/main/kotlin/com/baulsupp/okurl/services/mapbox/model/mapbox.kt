package com.baulsupp.okurl.services.mapbox.model

import com.baulsupp.okurl.location.Location
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class MapboxProperties(
  val address: String?,
  val category: String?,
  val tel: String?,
  val landmark: Boolean?,
  val maki: String?
)

@JsonClass(generateAdapter = true)
data class MapboxFeature(
  val id: String,
  val type: String,
  @Json(name = "place_type") val placeType: List<String>,
  val relevance: Double,
  val properties: MapboxProperties,
  val text: String,
  @Json(name = "place_name") val placeName: String,
  val center: Location
)

@JsonClass(generateAdapter = true)
data class MapboxGeometry(val coordinates: Location, val type: String)

@JsonClass(generateAdapter = true)
data class MapboxPlacesResult(
  val type: String,
  val query: List<String>,
  val features: List<MapboxFeature>,
  val geometry: MapboxGeometry?
)

@JsonClass(generateAdapter = true)
data class MapboxRoute(val geometry: String)

@JsonClass(generateAdapter = true)
data class MapboxDrivingResults(val routes: List<MapboxRoute>)

class MapboxLatLongAdapter {
  @ToJson fun toJson(value: Location): DoubleArray = doubleArrayOf(value.longitude, value.latitude)
  @FromJson fun fromJson(value: DoubleArray): Location = Location(value[0], value[1])
}
