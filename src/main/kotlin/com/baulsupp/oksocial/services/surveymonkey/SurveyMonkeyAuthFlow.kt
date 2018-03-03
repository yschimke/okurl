package com.baulsupp.oksocial.services.surveymonkey

import com.baulsupp.oksocial.NoToken
import com.baulsupp.oksocial.authenticator.SimpleWebServer
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object SurveyMonkeyAuthFlow {
  suspend fun login(client: OkHttpClient, outputHandler: OutputHandler<Response>, clientId: String,
                    clientSecret: String): Oauth2Token {
    SimpleWebServer.forCode().use { s ->
      val redirectUri = s.redirectUri

      val loginUrl = "https://api.surveymonkey.net/oauth/authorize?response_type=code&client_id=$clientId&redirect_uri=$redirectUri"

      outputHandler.openLink(loginUrl)

      val code = s.waitForCode()

      val body = FormBody.Builder().add("client_secret", clientSecret)
        .add("client_id", clientId)
        .add("code", code)
        .add("redirect_uri", redirectUri)
        .add("grant_type", "authorization_code")
        .build()

      val request = Request.Builder().url("https://api.surveymonkey.net/oauth/token")
        .post(body)
        .build()

      data class TokenResponse(val access_token: String)

      val response = client.query<TokenResponse>(request, NoToken)

      return Oauth2Token(response.access_token)
    }
  }
}
