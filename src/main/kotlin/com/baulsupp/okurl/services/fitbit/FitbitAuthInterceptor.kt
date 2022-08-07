package com.baulsupp.okurl.services.fitbit

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class FitbitAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.fitbit.com", "Fitbit API", "fitbit",
    "https://dev.fitbit.com/docs/", "https://dev.fitbit.com/apps/"
  )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Fitbit Client Id", "fitbit.clientId", "", false)
    val clientSecret = Secrets.prompt("Fitbit Client Secret", "fitbit.clientSecret", "", true)
    val scopes = Secrets.promptArray(
      "Scopes", "fitbit.scopes",
      listOf(
        "activity", "heartrate", "location", "nutrition", "profile",
        "settings", "sleep", "social", "weight"
      )
    )

    return FitbitAuthCodeFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.fitbit.com/1/user/-/profile.json",
        TokenValue(credentials), "user", "fullName"
      )
    )

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {
    val body = FormBody.Builder().add("grant_type", "refresh_token")
      .add("refresh_token", credentials.refreshToken!!)
      .build()
    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
    val request = Request.Builder().url("https://api.fitbit.com/oauth2/token")
      .post(body)
      .header("Authorization", basic)
      .build()

    val responseMap = client.queryMap<Any>(request)

    // TODO check if refresh token in response?
    return Oauth2Token(
      responseMap["access_token"] as String,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret
    )
  }
}
