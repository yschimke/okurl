package com.baulsupp.okurl.services.imgur

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object ImgurAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=code&state=x"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("client_id", clientId)
        .add("client_secret", clientSecret)
        .add("code", code)
        .add("grant_type", "authorization_code")
        .build()
      val request = Request.Builder().url("https://api.imgur.com/oauth2/token")
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
