package com.baulsupp.oksocial.services.instagram

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object InstagramAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String, scopes: Iterable<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://api.instagram.com/oauth/authorize/?client_id=$clientId&response_type=code&redirect_uri=${s.redirectUri}&scope=${scopes.joinToString(
        "+")}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val tokenUrl = "https://api.instagram.com/oauth/access_token"
      val body = FormBody.Builder().add("client_id", clientId)
        .add("redirect_uri", s.redirectUri)
        .add("client_secret", clientSecret)
        .add("code", code)
        .add("grant_type", "authorization_code")
        .build()
      val request = Request.Builder().url(tokenUrl).method("POST", body).build()

      val responseMap = AuthUtil.makeJsonMapRequest(client, request)

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
