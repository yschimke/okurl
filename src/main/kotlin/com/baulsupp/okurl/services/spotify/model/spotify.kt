package com.baulsupp.okurl.services.spotify.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Error(val status: Int, val message: String)

@JsonClass(generateAdapter = true)
data class ErrorResponse(val error: Error)
