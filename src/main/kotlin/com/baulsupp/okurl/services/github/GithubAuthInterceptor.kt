package com.baulsupp.okurl.services.github

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
import com.baulsupp.okurl.kotlin.queryMapValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * https://developer.github.com/docs/authentication
 */
class GithubAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.github.com", "Github API", "github",
    "https://developer.github.com/v3/", "https://github.com/settings/developers"
  )

  override suspend fun intercept(
    chain: Interceptor.Chain,
    credentials: Oauth2Token?,
    credentialsStore: CredentialsStore
  ): Response {
    return if (credentials != null) {
      return intercept(chain, credentials)
    } else {
      withContext(Dispatchers.IO) {
        chain.proceed(addPreviewHeader(chain.request()))
      }
    }
  }

  private fun addPreviewHeader(request: Request): Request {
    // https://github.com/octokit/octokit.rb/blob/master/lib/octokit/preview.rb

    val previews = listOf(
      "application/vnd.github.doctor-strange-preview+json",
      "application/vnd.github.luke-cage-preview+json",
      "application/vnd.github.antiope-preview+json",
      "application/vnd.github.cloak-preview+json",
      "application/vnd.github.groot-preview+json",
      "application/vnd.github.groot-preview+json",
      "application/vnd.github.wyandotte-preview+json",
      "application/vnd.github.drax-preview+json",
      "application/vnd.github.barred-rock-preview",
      "application/vnd.github.squirrel-girl-preview",
      "application/vnd.github.nightshade-preview+json",
      "application/vnd.github.mockingbird-preview+json",
      "application/vnd.github.hellcat-preview+json",
      "application/vnd.github.mister-fantastic-preview+json",
      "application/vnd.github.inertia-preview+json",
      "application/vnd.github.spiderman-preview",
      "application/vnd.github.mercy-preview+json",
      "application/vnd.github.black-panther-preview+json",
      "application/vnd.github.speedy-preview+json",
      "application/vnd.github.shadow-cat-preview",
      "application/vnd.github.baptiste-preview+json",
      "application/vnd.github.gambit-preview+json",
      "application/vnd.github.starfox-preview+json",
      "application/vnd.github.dorian-preview+json",
    )

    return request.newBuilder()
      .apply {
        previews.forEach {
          addHeader("Accept", it)
        }
      }
      .build()
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    val token = credentials.accessToken

    val request = addPreviewHeader(chain.request())
      .newBuilder().addHeader("Authorization", "token $token").build()

    return chain.proceed(request)
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter = GithubApiCompleter(client, this, credentialsStore)

  override fun apiDocPresenter(
    url: String,
    client: OkHttpClient
  ): ApiDocPresenter = GithubApiDocPresenter()

  override fun authFlow() = GithubAuthFlow(serviceDefinition)

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.queryMapValue<String>(
        "https://api.github.com/user",
        TokenValue(credentials), "name"
      )
    )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.github.com", "uploads.github.com")
}
