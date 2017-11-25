package com.baulsupp.oksocial.services.spotify

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

object SpotifyAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<*>, clientId: String,
            clientSecret: String, scopes: Iterable<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      val loginUrl = "https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&state=x&redirect_uri=${s.redirectUri}&scope=$scopesString"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://accounts.spotify.com/api/token"
      val body = FormBody.Builder().add("client_id", clientId)
          .add("redirect_uri", s.redirectUri)
          .add("code", code)
          .add("grant_type", "authorization_code")
          .build()
      val request = Request.Builder().header("Authorization",
          Credentials.basic(clientId, clientSecret))
          .url(tokenUrl)
          .method("POST", body)
          .build()

      val responseMap = AuthUtil.makeJsonMapRequest(client, request)

      return Oauth2Token(responseMap["access_token"] as String,
          responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
