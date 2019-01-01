package com.baulsupp.okurl.services.lyft

import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object LyftClientAuthFlow {
  suspend fun login(client: OkHttpClient, clientId: String, clientSecret: String): Oauth2Token {
    val body = RequestBody.create(
      MediaType.get("application/json"),
      "{\"grant_type\": \"client_credentials\", \"scope\": \"public\"}"
    )
    val basic = Credentials.basic(clientId, clientSecret)
    val request = Request.Builder().url("https://api.lyft.com/oauth/token")
      .post(body)
      .header("Authorization", basic)
      .build()

    val responseMap = client.queryMap<Any>(request)

    // TODO refreshable without refresh token
    return Oauth2Token(responseMap["access_token"] as String)
  }
}
