package com.baulsupp.oksocial.services.microsoft

import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.services.microsoft.model.Token
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object MicrosoftAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String, scopes: List<String>): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=$clientId&response_type=code&redirect_uri=${s.redirectUri}&scope=${scopes.joinToString("+")}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val url = HttpUrl.parse("https://login.microsoftonline.com/common/oauth2/v2.0/token")

      val body = FormBody.Builder().add("grant_type", "authorization_code")
        .add("redirect_uri", s.redirectUri)
        .add("client_id", clientId)
        .add("client_secret", clientSecret)
        .add("code", code)
        .build()

      val request = Request.Builder().url(url!!).post(body).build()

      val responseMap = client.query<Token>(request)

      return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
    }
  }
}
