package com.baulsupp.okurl.authenticator.oauth2

data class Oauth2Token(
  val accessToken: String,
  val refreshToken: String? = null,
  val clientId: String? = null,
  val clientSecret: String? = null
) {
  fun isRenewable(): Boolean = refreshToken != null && clientId != null && clientSecret != null
}
