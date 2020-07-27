package com.baulsupp.okurl.services.lyft

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * https://developer.lyft.com/docs/authentication
 */
class LyftAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.lyft.com", "Lyft API", "lyft",
    "https://developer.lyft.com/docs", "https://www.lyft.com/developers/manage"
  )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Lyft Client Id", "lyft.clientId", "", false)
    val clientSecret = Secrets.prompt("Lyft Client Secret", "lyft.clientSecret", "", true)

    return if (authArguments == listOf("--client")) {
      LyftClientAuthFlow.login(client, clientId, clientSecret)
    } else {
      val scopes = Secrets.promptArray(
        "Scopes", "lyft.scopes", listOf(
          "public",
          "rides.read",
          "offline",
          "rides.request",
          "profile"
        )
      )

      LyftAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
    }
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.lyft.com/v1/profile",
        TokenValue(credentials), "id"
      )
    )

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token {

    val body = ("{\"grant_type\": \"refresh_token\", \"refresh_token\": \"" +
      credentials.refreshToken + "\"}"
      ).toRequestBody("application/json".toMediaType())
    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
    val request = Request.Builder().url("https://api.lyft.com/oauth/token")
      .post(body)
      .header("Authorization", basic)
      .build()

    val responseMap = client.queryMap<Any>(request)

    return Oauth2Token(
      responseMap["access_token"] as String,
      credentials.refreshToken, credentials.clientId,
      credentials.clientSecret
    )
  }
}
