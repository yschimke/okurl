package com.baulsupp.okurl.services.box

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
import com.baulsupp.okurl.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.box.model.FolderItems
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BoxAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override val serviceDefinition = Oauth2ServiceDefinition("api.box.com", "Box API", "box",
    "https://developer.box.com/reference", "https://app.box.com/developers/console/")

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

    val clientId = Secrets.prompt("Box Client Id", "box.clientId", "", false)
    val clientSecret = Secrets.prompt("Box Client Secret", "box.clientSecret", "", true)

    val scopes = Secrets.promptArray("Scopes", "box.scopes", listOf("root_readwrite"))

    return BoxAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: Oauth2Token
  ): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.box.com/2.0/users/me",
      TokenValue(credentials), "name"))

  override fun canRenew(credentials: Oauth2Token): Boolean = false

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "file_id") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<FolderItems>(
          "https://api.box.com/2.0/folders/0/items",
          tokenSet).entries.filter { it.type == "file" }.map { it.id }
      }
    }

    completer.withCachedVariable(name(), "folder_id") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        listOf("0") + client.query<FolderItems>(
          "https://api.box.com/2.0/folders/0/items",
          tokenSet).entries.filter { it.type == "folder" }.map { it.id }
      }
    }

    return completer
  }

  override fun hosts(credentialsStore: CredentialsStore): Set<String> = setOf("api.box.com")
}
