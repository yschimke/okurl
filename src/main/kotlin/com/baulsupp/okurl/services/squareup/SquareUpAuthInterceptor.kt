package com.baulsupp.okurl.services.squareup

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.squareup.model.LocationList
import com.baulsupp.okurl.services.squareup.model.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class SquareUpAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "connect.squareup.com", "SquareUp API", "squareup",
    "https://docs.connect.squareup.com/api/connect/v2/",
    "https://connect.squareup.com/apps"
  )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val reqBuilder = request.newBuilder().addHeader("Authorization", "Bearer $token")
    if (request.header("Accept") == null) {
      reqBuilder.addHeader("Accept", "application/json")
    }
    request = reqBuilder.build()

    return chain.proceed(request)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.query<User>(
        "https://connect.squareup.com/v1/me",
        TokenValue(credentials)
      ).name
    )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("SquareUp Application Id", "squareup.clientId", "", false)
    val clientSecret = Secrets.prompt(
      "SquareUp Application Secret", "squareup.clientSecret", "",
      true
    )
    val scopes = Secrets.promptArray(
      "Scopes", "squareup.scopes", listOf(
        "MERCHANT_PROFILE_READ",
        "PAYMENTS_READ",
        "SETTLEMENTS_READ",
        "BANK_ACCOUNTS_READ"
      )
    )

    return SquareUpAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "location") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<LocationList>(
          "https://connect.squareup.com/v2/locations",
          tokenSet
        ).locations.map { it.id }
      }
    }

    return completer
  }
}
