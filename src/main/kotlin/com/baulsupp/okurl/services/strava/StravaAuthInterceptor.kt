package com.baulsupp.okurl.services.strava

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.kotlin.form
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.strava.model.Athlete
import okhttp3.OkHttpClient
import okhttp3.Response

class StravaAuthInterceptor : Oauth2AuthInterceptor() {
  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Strava Client Id", "strava.clientId", "", false)
    val clientSecret = Secrets.prompt("Strava Client Secret", "strava.clientSecret", "", true)

    val scopes = Secrets.promptArray(
      "Scopes", "strava.scopes", listOf(
        "read_all",
        "profile:write",
        "activity:write"
      )
    )

    return StravaAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override val serviceDefinition = Oauth2ServiceDefinition(
    "www.strava.com", "Strava", "strava",
    "https://developers.strava.com/docs/reference/", "https://www.strava.com/settings/api"
  )

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(client.query<Athlete>("https://www.strava.com/api/v3/athlete").username)

  override fun canRenew(credentials: Oauth2Token): Boolean {
    return credentials.isRenewable()
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val responseMap = client.query<AuthResponse>(request("https://www.strava.com/oauth/token") {
      form {
        add("grant_type", "refresh_token")
        add("client_id", credentials.clientId!!)
        add("client_secret", credentials.clientSecret!!)
        add("refresh_token", credentials.refreshToken!!)
      }
    })

    return Oauth2Token(
      responseMap.access_token,
      responseMap.refresh_token,
      credentials.clientId,
      credentials.clientSecret
    )
  }
}
