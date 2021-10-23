package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Token(
    val access_token: String,
    val cobranding: List<Any>,
    val onboarding: Int,
    val promotions: List<Any>,
    val state: Any?,
    val token_type: String,
    val user: User
)
