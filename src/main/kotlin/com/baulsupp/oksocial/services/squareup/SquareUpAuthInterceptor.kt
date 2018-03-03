package com.baulsupp.oksocial.services.squareup

import com.baulsupp.oksocial.Token
import com.baulsupp.oksocial.TokenValue
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.squareup.model.LocationList
import com.baulsupp.oksocial.services.squareup.model.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.Arrays

class SquareUpAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("connect.squareup.com", "SquareUp API", "squareup",
      "https://docs.connect.squareup.com/api/connect/v2/",
      "https://connect.squareup.com/apps")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val reqBuilder = request.newBuilder().addHeader("Authorization", "Bearer " + token)
    if (request.header("Accept") == null) {
      reqBuilder.addHeader("Accept", "application/json")
    }
    request = reqBuilder.build()

    return chain.proceed(request)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.query<User>("https://connect.squareup.com/v1/me", TokenValue(credentials)).name)

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("SquareUp Application Id", "squareup.clientId", "", false)
    val clientSecret = Secrets.prompt("SquareUp Application Secret", "squareup.clientSecret", "",
      true)
    val scopes = Secrets.promptArray("Scopes", "squareup.scopes", Arrays.asList(
      "MERCHANT_PROFILE_READ",
      "PAYMENTS_READ",
      "SETTLEMENTS_READ",
      "BANK_ACCOUNTS_READ"
    ))

    return SquareUpAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache,
                            tokenSet: Token): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "location", {
      credentialsStore.get(serviceDefinition(), tokenSet)?.let {
        client.query<LocationList>(
          "https://connect.squareup.com/v2/locations",
          tokenSet).locations.map { it.id }
      }
    })

    return completer
  }

  override fun hosts(): Set<String> = setOf("connect.squareup.com")
}
