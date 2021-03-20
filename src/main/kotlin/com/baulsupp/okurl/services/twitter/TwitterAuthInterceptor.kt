package com.baulsupp.okurl.services.twitter

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.okurl.openapi.OpenApiDocPresenter
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.twitter.joauth.Signature
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TwitterAuthInterceptor : AuthInterceptor<TwitterCredentials>() {

  override val serviceDefinition = TwitterServiceDefinition()

  override suspend fun intercept(chain: Interceptor.Chain, credentials: TwitterCredentials): Response {
    var request = chain.request()

    val authHeader = Signature().generateAuthorization(request, credentials)
    request = request.newBuilder().addHeader("Authorization", authHeader).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): TwitterCredentials {
    val consumerKey = Secrets.prompt("Consumer Key", "twitter.consumerKey", "", false)
    val consumerSecret = Secrets.prompt("Consumer Secret", "twitter.consumerSecret", "", true)

    return WebAuthorizationFlow(client, outputHandler).authorise(consumerKey, consumerSecret)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: TwitterCredentials
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.twitter.com/1.1/account/verify_credentials.json",
        TokenValue(credentials), "name"
      )
    )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = TwitterUtil.TWITTER_API_HOSTS

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter = TwitterApiCompleter(client, credentialsStore, completionVariableCache)

  override fun apiDocPresenter(
    url: String,
    client: OkHttpClient
  ): ApiDocPresenter = OpenApiDocPresenter(
    "https://raw.githubusercontent.com/APIs-guru/openapi-directory/master/APIs/twitter.com/legacy/1.1/swagger.yaml".toHttpUrl(),
    client
  )
}
