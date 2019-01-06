package com.baulsupp.okurl.services.transferwise

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.kotlin.requestBuilder
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response

abstract class BaseTransferwiseAuthInterceptor : Oauth2AuthInterceptor() {
  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Transferwise Client Id", "transferwise.clientId", "", false)
    val clientSecret = Secrets.prompt(
      "Transferwise Client Secret", "transferwise.clientSecret", "",
      true
    )

    return TransferwiseAuthFlow.login(client, outputHandler, serviceDefinition.apiHost(), clientId, clientSecret)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.transferwise.com/v1/me",
        TokenValue(credentials), "name"
      )
    )

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {

    val body = FormBody.Builder()
      .add("grant_type", "refresh_token")
      .add("refresh_token", credentials.refreshToken!!)
      .build()
    val basic = Credentials.basic(credentials.clientId!!, credentials.clientSecret!!)
    val request = requestBuilder(
      "https://" + serviceDefinition.apiHost() + "/oauth/token",
      NoToken
    )
      .post(body)
      .header("Authorization", basic)
      .build()

    val responseMap = client.queryMap<Any>(request)

    // TODO check if refresh token in response?
    return Oauth2Token(
      responseMap["access_token"] as String,
      responseMap["refresh_token"] as String, credentials.clientId,
      credentials.clientSecret
    )
  }
}
