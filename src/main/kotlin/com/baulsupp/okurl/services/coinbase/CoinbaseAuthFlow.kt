package com.baulsupp.okurl.services.coinbase

import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object CoinbaseAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: Iterable<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val loginUrl = "https://www.coinbase.com/oauth/authorize?response_type=code&client_id=$clientId&redirect_uri=${s.redirectUri}&scope=${scopes.joinToString(",")}&account=all"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder()
        .add("client_id", clientId)
        .add("client_secret", clientSecret)
        .add("redirect_uri", s.redirectUri)
        .add("code", code)
        .add("grant_type", "authorization_code")
        .build()

      val request = Request.Builder().url("https://api.coinbase.com/oauth/token").method("POST", body).build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(responseMap["access_token"] as String, responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
