package com.baulsupp.okurl.services.postman

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.readPasswordString
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
import com.baulsupp.okurl.services.postman.model.CollectionsResult
import com.baulsupp.okurl.services.postman.model.UserResult
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class PostmanAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "api.getpostman.com", "Postman API", "postman",
    "https://docs.api.getpostman.com/",
    "https://app.getpostman.com/dashboard/integrations"
  )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("X-Api-Key", token).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {
    outputHandler.openLink("https://app.getpostman.com/dashboard/integrations/pm_pro_api/list")

    val token = System.console().readPasswordString("Enter Token: ")

    return Oauth2Token(token)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(
      client.query<UserResult>(
        "https://api.getpostman.com/me",
        TokenValue(credentials)
      ).user.id
    )

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.getpostman.com")

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "collection_uid") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<CollectionsResult>(
          "https://api.getpostman.com/collections",
          tokenSet
        ).collections.map { it.id }
      }
    }

    return completer
  }
}
