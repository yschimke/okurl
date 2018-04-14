package com.baulsupp.oksocial.services.github

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder

object GithubAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: Iterable<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      val loginUrl = "https://github.com/login/oauth/authorize?client_id=$clientId&scope=$scopesString&redirect_uri=${s.redirectUri}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("client_id", clientId)
        .add("client_id", clientId)
        .add("code", code)
        .add("client_secret", clientSecret)
        .add("redirect_uri", s.redirectUri)
        .build()
      val request = Request.Builder().url("https://github.com/login/oauth/access_token")
        .header("Accept", "application/json")
        .post(body)
        .build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
