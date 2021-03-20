package com.baulsupp.okurl.services.imgur

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class ImgurAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.imgur.com", "Imgur API", "imgur",
    "https://api.imgur.com/endpoints", "https://imgur.com/account/settings/apps"
  )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Imgur Client Id", "imgur.clientId", "", false)
    val clientSecret = Secrets.prompt("Imgur Client Secret", "imgur.clientSecret", "", true)

    return ImgurAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.imgur.com/3/account/me",
        TokenValue(credentials), "data", "url"
      )
    )

  override fun canRenew(result: Response): Boolean = result.code == 403

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
    val body = FormBody.Builder().add("refresh_token", credentials.refreshToken!!)
      .add("client_id", credentials.clientId!!)
      .add("client_secret", credentials.clientSecret!!)
      .add("grant_type", "refresh_token")
      .build()
    val request = Request.Builder().url("https://api.imgur.com/oauth2/token")
      .method("POST", body)
      .build()

    val responseMap = client.queryMap<Any>(request)

    return Oauth2Token(
      responseMap["access_token"] as String,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret
    )
  }
}
