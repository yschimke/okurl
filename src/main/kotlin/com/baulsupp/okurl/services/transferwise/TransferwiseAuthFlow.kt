package com.baulsupp.okurl.services.transferwise

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.requestBuilder
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response

object TransferwiseAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    host: String,
    clientId: String,
    clientSecret: String
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val serverUri = s.redirectUri

      val loginUrl =
        "https://$host/oauth/authorize?client_id=$clientId&response_type=code&scope=transfers&redirect_uri=$serverUri"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("client_id", clientId).add("redirect_uri", serverUri)
        .add("grant_type", "authorization_code").add("code", code).build()
      val basic = Credentials.basic(clientId, clientSecret)
      val request = requestBuilder(
        "https://$host/oauth/token",
        NoToken
      )
        .post(body)
        .header("Authorization", basic)
        .build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(
        responseMap["access_token"] as String,
        responseMap["refresh_token"] as String, clientId, clientSecret
      )
    }
  }
}
