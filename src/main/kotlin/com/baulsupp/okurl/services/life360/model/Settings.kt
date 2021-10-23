package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Settings(
    val alerts: Alerts,
    val dateFormat: String,
    val locale: String,
    val map: Map,
    val timeZone: String,
    val unitOfMeasure: String,
    val zendrive: Any?
)
