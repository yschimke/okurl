package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Communication(
    val channel: String,
    val type: String?,
    val value: String
)
