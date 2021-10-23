package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Map(
    val advisor: String,
    val crime: String,
    val crimeDuration: String,
    val family: String,
    val fire: String,
    val hospital: String,
    val memberRadius: String,
    val placeRadius: String,
    val police: String,
    val sexOffenders: String
)
