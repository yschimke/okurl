package com.baulsupp.okurl.authenticator.oauth2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Oauth2TokenResponse(val access_token: String)
