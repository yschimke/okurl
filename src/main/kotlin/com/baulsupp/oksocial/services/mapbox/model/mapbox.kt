package com.baulsupp.oksocial.services.mapbox.model

import com.baulsupp.oksocial.location.Location
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson

data class MapboxProperties(val address: String?, val category: String?, val tel: String?, val landmark: Boolean?, val maki: String?)

data class MapboxFeature(val id: String, val type: String, @Json(name = "place_type") val placeType: List<String>, val relevance: Double, val properties: MapboxProperties, val text: String, @Json(name = "place_name") val placeName: String, val center: Location)

data class MapboxGeometry(val coordinates: Location, val type: String)

data class MapboxPlacesResult(val type: String, val query: List<String>, val features: List<MapboxFeature>, val geometry: MapboxGeometry?)

data class MapboxRoute(val geometry: String)

data class MapboxDrivingResults(val routes: List<MapboxRoute>)

class MapboxLatLongAdapter {
  @ToJson
  fun toJson(videoSize: Location): DoubleArray {
    return doubleArrayOf(videoSize.longitude, videoSize.latitude)
  }

  @FromJson

  fun fromJson(dimensions: DoubleArray): Location {
    if (dimensions.size != 2) {
      throw Exception("Expected 2 elements but was $dimensions")
    }
    val longitude = dimensions[0]
    val latitude = dimensions[1]
    return Location(latitude, longitude)
  }
}
