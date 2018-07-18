package com.baulsupp.okurl.services.slack

import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder

object SlackAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: Iterable<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val scopesString = URLEncoder.encode(scopes.joinToString(" "), "UTF-8")

      val loginUrl = "https://slack.com/oauth/authorize?client_id=$clientId&redirect_uri=${s.redirectUri}&scope=$scopesString"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val url = HttpUrl.parse("https://api.slack.com/api/oauth.access")!!
        .newBuilder()
        .addQueryParameter("client_id", clientId)
        .addQueryParameter("client_secret", clientSecret)
        .addQueryParameter("redirect_uri", s.redirectUri)
        .addQueryParameter("code", code)
        .build()

      val request = Request.Builder().url(url).build()

      val responseMap = client.queryMap<Any>(request)

      // TODO bot and user flow
      // {ok=true, access_token=xxx, scope=a,b,c, user_id=xxx, team_name=xxxx, team_id=xxx, bot={bot_user_id=xxxx, bot_access_token=xxx}}

      if (responseMap["ok"] != true) {
        throw IOException("authorization failed: " + responseMap["error"])
      }

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
