package com.baulsupp.okurl.services.quip

import com.baulsupp.okurl.authenticator.AuthInterceptor
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
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.readPasswordString
import com.baulsupp.okurl.services.quip.model.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class QuipAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("platform.quip.com", "Quip API", "quip",
    "https://quip.com/dev/automation/documentation",
    "https://quip.com/dev/token")

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

    outputHandler.openLink("https://quip.com/dev/token")

    val token = System.console().readPasswordString("Enter Token: ")

    return Oauth2Token(token)
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

    completer.withCachedVariable(name(), "folderId") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        currentUser(client, tokenSet).let {
          listOfNotNull(it.starred_folder_id, it.private_folder_id, it.desktop_folder_id,
            it.archive_folder_id) + it.shared_folder_ids.orEmpty()
        }
      }
    }

    return completer
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(currentUser(client,
      TokenValue(credentials)).name)

  private suspend fun currentUser(client: OkHttpClient, tokenSet: Token) =
    client.query<User>("https://platform.quip.com/1/users/current", tokenSet)

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("platform.quip.com")
}
