package com.baulsupp.okurl.services.google.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthError(val error: String, val error_description: String)
