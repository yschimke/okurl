package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberFeatures(
    val device: String,
    val disconnected: String,
    val geofencing: String,
    val mapDisplay: String,
    val nonSmartphoneLocating: String,
    val pendingInvite: String,
    val shareLocation: String,
    val shareOffTimestamp: Any?,
    val smartphone: String
)
