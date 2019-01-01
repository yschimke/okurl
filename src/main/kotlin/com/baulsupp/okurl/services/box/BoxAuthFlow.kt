package com.baulsupp.okurl.services.box

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.requestBuilder
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URLEncoder

object BoxAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: List<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      val loginUrl =
        "https://account.box.com/api/oauth2/authorize?client_id=$clientId&scope=$scopesString&response_type=code&state=x&redirect_uri=${s.redirectUri}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("grant_type", "authorization_code").add("code", code).add("client_id", clientId)
        .add("client_secret", clientSecret).build()
      val request = requestBuilder(
        "https://api.box.com/oauth2/token",
        NoToken
      )
        .post(body)
        .build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(
        responseMap["access_token"] as String,
        responseMap["refresh_token"] as String, clientId, clientSecret
      )
    }
  }
}
