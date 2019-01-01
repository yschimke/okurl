package com.baulsupp.okurl.services.microsoft

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.SimpleWebServer
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.services.microsoft.model.Token
import okhttp3.OkHttpClient
import okhttp3.Response

object MicrosoftAuthFlow {
  suspend fun login(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    clientId: String,
    clientSecret: String,
    scopes: List<String>
  ): Oauth2Token {
    SimpleWebServer.forCode().use { s ->

      val loginUrl =
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?" +
          "client_id=$clientId&" +
          "response_type=code&" +
          "redirect_uri=${s.redirectUri}&" +
          "scope=${scopes.joinToString("+")}"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val responseMap = client.query<Token>(request("https://login.microsoftonline.com/common/oauth2/v2.0/token") {
        post(
          form {
            add("grant_type", "authorization_code")
            add("redirect_uri", s.redirectUri)
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("code", code)
          }
        )
      })

      return Oauth2Token(responseMap.access_token, responseMap.refresh_token, clientId, clientSecret)
    }
  }
}
