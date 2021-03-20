package com.baulsupp.okurl.services.dropbox

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.queryMap
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object DropboxAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl =
        "https://www.dropbox.com/1/oauth2/authorize?client_id=$clientId&response_type=code&redirect_uri=${s.redirectUri}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val basic = Credentials.basic(clientId, clientSecret)
      val body = FormBody.Builder().add("code", code)
        .add("grant_type", "authorization_code")
        .add("redirect_uri", s.redirectUri)
        .build()
      val request = Request.Builder().url("https://api.dropboxapi.com/1/oauth2/token")
        .post(body)
        .header("Authorization", basic)
        .build()

      val responseMap = client.queryMap<Any>(request)

      return Oauth2Token(responseMap["access_token"] as String)
    }
  }
}
