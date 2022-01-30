package com.baulsupp.okurl.services.fitbit

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder

object FitbitAuthCodeFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: Iterable<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      val loginUrl =
        "https://www.fitbit.com/oauth2/authorize?client_id=$clientId&response_type=code&redirect_uri=${URLEncoder.encode(
          s.redirectUri, "UTF-8"
        )}&scope=$scopesString"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val basic = Credentials.basic(clientId, clientSecret)

      val body = FormBody.Builder().add("client_id", clientId)
        .add("grant_type", "authorization_code")
        .add("code", code)
        .add("redirect_uri", s.redirectUri)
        .build()
      val request = Request.Builder().url("https://api.fitbit.com/oauth2/token")
        .header("Authorization", basic)
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
