package com.baulsupp.okurl.services.uber

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.NoToken
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMap
import com.baulsupp.okurl.kotlin.requestBuilder
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class UberAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition =
    Oauth2ServiceDefinition(host(), "Uber API", "uber",
      "https://developer.uber.com/docs/riders/references/api",
      "https://developer.uber.com/dashboard/")

  private fun host(): String = "api.uber.com"

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer $token").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Uber Client Id", "uber.clientId", "", false)
    val clientSecret = Secrets.prompt("Uber Client Secret", "uber.clientSecret", "", true)

    return UberAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    return BaseUrlCompleter(UrlList.fromResource(name())!!, hosts(credentialsStore), completionVariableCache)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials {
    val map = client.queryMap<Any>("https://api.uber.com/v1/me",
      TokenValue(credentials))
    return ValidatedCredentials("${map["first_name"]} ${map["last_name"]}")
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.uber.com", "login.uber.com", "sandbox-api.uber.com")

  override fun canRenew(credentials: Oauth2Token): Boolean = credentials.isRenewable()

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val tokenUrl = "https://login.uber.com/oauth/v2/token"

    val body = FormBody.Builder().add("client_id", credentials.clientId!!)
      .add("client_secret", credentials.clientSecret!!)
      .add("refresh_token", credentials.refreshToken!!)
      .add("grant_type", "refresh_token")
      .build()

    val request = requestBuilder(tokenUrl, NoToken).method("POST", body).build()

    val responseMap = client.queryMap<Any>(request)

    return Oauth2Token(responseMap["access_token"] as String,
      responseMap["refresh_token"] as String, credentials.clientId,
      credentials.clientSecret)
  }
}
