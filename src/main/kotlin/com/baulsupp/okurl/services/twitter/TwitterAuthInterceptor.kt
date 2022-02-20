package com.baulsupp.okurl.services.twitter

import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.Oauth2AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.authenticator.oauth2.Oauth2Token
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.openapi.OpenApiCompleter
import com.baulsupp.okurl.openapi.OpenApiDocPresenter
import com.baulsupp.okurl.services.twitter.model.UserResponse2
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

class TwitterAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.twitter.com", "Twitter API", "twitter",
    "https://developer.twitter.com/en/docs/api-reference-index",
    "https://developer.twitter.com/en/apps"
  )

  override fun authFlow() = TwitterAuthFlow(serviceDefinition)

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.query<UserResponse2>(
        "https://api.twitter.com/2/users/me",
        TokenValue(credentials)
      ).data.name
    )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf(
    "api.twitter.com", "upload.twitter.com", "stream.twitter.com", "mobile.twitter.com",
    "syndication.twitter.com", "pbs.twimg.com", "t.co", "userstream.twitter.com",
    "sitestream.twitter.com", "search.twitter.com"
  )

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter = OpenApiCompleter("https://api.twitter.com/2/openapi.json".toHttpUrl(), client)

  override fun apiDocPresenter(
    url: String,
    client: OkHttpClient
  ): ApiDocPresenter = OpenApiDocPresenter(
    "https://api.twitter.com/2/openapi.json".toHttpUrl(),
    client
  )
}
