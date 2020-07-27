package com.baulsupp.okurl.services.cooee

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfo(val token: String, val name: String, val email: String)
