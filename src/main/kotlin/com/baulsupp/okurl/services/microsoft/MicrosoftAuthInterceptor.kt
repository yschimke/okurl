package com.baulsupp.okurl.services.microsoft

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
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.microsoft.model.DriveRootList
import com.baulsupp.okurl.services.microsoft.model.Token
import com.baulsupp.okurl.services.microsoft.model.User
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * https://graph.microsoft.io/en-us/docs/authorization/app_authorization
 * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
 */
class MicrosoftAuthInterceptor : Oauth2AuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "graph.microsoft.com", "Microsoft API", "microsoft",
    "https://graph.microsoft.io/en-us/docs/get-started/rest",
    "https://apps.dev.microsoft.com/#/appList"
  )

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): Oauth2Token {

    val clientId = Secrets.prompt("Microsoft Client Id", "microsoft.clientId", "", false)
    val clientSecret = Secrets.prompt("Microsoft Client Secret", "microsoft.clientSecret", "", true)

    val scopes = Secrets.promptArray(
      "Scopes", "microsoft.scopes", listOf(
        "User.Read",
        "Contacts.Read",
        "Calendars.Read",
        "Mail.Read",
        "email",
        "offline_access",
        "openid",
        "profile",
        "Files.ReadWrite"
      )
    )

    return MicrosoftAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {

    val body = FormBody.Builder().add("grant_type", "refresh_token")
      .add("redirect_uri", "http://localhost:3000/callback")
      .add("client_id", credentials.clientId!!)
      .add("client_secret", credentials.clientSecret!!)
      .add("refresh_token", credentials.refreshToken!!)
      .build()

    val request = Request.Builder().url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
      .post(body)
      .build()

    val responseMap = client.query<Token>(request)

    return Oauth2Token(
      responseMap.access_token,
      responseMap.refresh_token,
      credentials.clientId,
      credentials.clientSecret
    )
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: com.baulsupp.okurl.credentials.Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withCachedVariable(name(), "itemId") {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        client.query<DriveRootList>("https://graph.microsoft.com/v1.0/me/drive/root/children", tokenSet)
          .value.map { it.id }
      }
    }

    return completer
  }

  override suspend fun validate(client: OkHttpClient, credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.query<User>("https://graph.microsoft.com/v1.0/me", TokenValue(credentials)).displayName)
}
