package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Issues(
    val action: Any?,
    val dialog: Any?,
    val disconnected: String,
    val status: Any?,
    val title: Any?,
    val troubleshooting: String,
    val type: String?
)
