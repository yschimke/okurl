package com.baulsupp.okurl.services.spotify

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder

object SpotifyAuthFlow {
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
        "https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&state=x&redirect_uri=${s.redirectUri}&scope=$scopesString"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://accounts.spotify.com/api/token"
      val body = FormBody.Builder().add("client_id", clientId)
        .add("redirect_uri", s.redirectUri)
        .add("code", code)
        .add("grant_type", "authorization_code")
        .build()
      val request = Request.Builder().header(
        "Authorization",
        Credentials.basic(clientId, clientSecret)
      )
        .url(tokenUrl)
        .method("POST", body)
        .build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(
        responseMap["access_token"] as String,
        responseMap["refresh_token"] as String, clientId, clientSecret
      )
    }
  }
}
