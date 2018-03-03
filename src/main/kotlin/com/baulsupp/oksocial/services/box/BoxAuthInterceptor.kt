package com.baulsupp.oksocial.services.box

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
import com.baulsupp.oksocial.kotlin.queryMapValue
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.box.model.FolderItems
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class BoxAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("api.box.com", "Box API", "box",
      "https://developer.box.com/reference", "https://app.box.com/developers/console/")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer $token").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Box Client Id", "box.clientId", "", false)
    val clientSecret = Secrets.prompt("Box Client Secret", "box.clientSecret", "", true)

    val scopes = Secrets.promptArray("Scopes", "box.scopes", listOf("root_readwrite"))

    return BoxAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.queryMapValue<String>("https://api.box.com/2.0/users/me", TokenValue(credentials), "name"))

  override fun canRenew(credentials: Oauth2Token): Boolean = false

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache,
                            tokenSet: Token): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "file_id", {
      credentialsStore.get(serviceDefinition(), tokenSet)?.let {
        client.query<FolderItems>(
          "https://api.box.com/2.0/folders/0/items",
          tokenSet).entries.filter { it.type == "file" }.map { it.id }
      }
    })

    completer.withCachedVariable(name(), "folder_id", {
      credentialsStore.get(serviceDefinition(), tokenSet)?.let {
        listOf("0") + client.query<FolderItems>(
          "https://api.box.com/2.0/folders/0/items",
          tokenSet).entries.filter { it.type == "folder" }.map { it.id }
      }
    })

    return completer
  }

  override fun hosts(): Set<String> = setOf("api.box.com")
}
