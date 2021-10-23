package com.baulsupp.okurl.services.life360.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val avatar: String,
    val avatarAuthor: Any?,
    val cobranding: List<Any>,
    val communications: List<Communication>,
    val created: String,
    val firstName: String,
    val id: String,
    val language: String,
    val lastName: String,
    val locale: String,
    val loginEmail: String,
    val loginPhone: String,
    val settings: Settings
)
