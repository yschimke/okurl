package com.baulsupp.okurl.services.soundcloud

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object SoundcloudAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val redirectUri = "https://wear.googleapis.com/3p_auth/com.google.wear.maf.soundcloud?code=xyz`"

      val x = "https%3A%2F%2Fwear.googleapis.com%2F3p_auth%2Fcom.google.wear.maf.soundcloud%3Fcode%3Dxyz%60"

      val loginUrl =
        "https://api.soundcloud.com/connect?client_id=${clientId}&redirect_uri=${x}&response_type=code&state=dummy"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("code", code)
        .add("grant_type", "authorization_code")
        .add("redirect_uri", redirectUri)
        .add("client_id", clientId)
        .add("client_secret", clientSecret)
        .build()
      val request = Request.Builder().url("https://api.soundcloud.com/oauth2/token")
        .post(body)
        .build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(responseMap["access_token"] as String, responseMap["refresh_token"] as String, clientId, clientSecret)
    }
  }
}
