package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(
    val accuracy: String,
    val address1: String?,
    val address2: String,
    val battery: String,
    val charge: String,
    val driveSDKStatus: Any?,
    val endTimestamp: String,
    val inTransit: String,
    val isDriving: String,
    val latitude: String,
    val longitude: String,
    val name: String?,
    val placeType: Any?,
    val shortAddress: String,
    val since: Int,
    val source: String?,
    val sourceId: String?,
    val speed: Int,
    val startTimestamp: Int,
    val timestamp: String,
    val tripId: Any?,
    val userActivity: Any?,
    val wifiState: String
)
