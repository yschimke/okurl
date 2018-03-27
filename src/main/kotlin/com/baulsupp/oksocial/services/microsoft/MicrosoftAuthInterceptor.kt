package com.baulsupp.oksocial.services.microsoft

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.TokenValue
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.microsoft.model.DriveRootList
import com.baulsupp.oksocial.services.microsoft.model.Token
import com.baulsupp.oksocial.services.microsoft.model.User
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.Arrays

/**
 * https://graph.microsoft.io/en-us/docs/authorization/app_authorization
 * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
 */
class MicrosoftAuthInterceptor : AuthInterceptor<Oauth2Token>() {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("graph.microsoft.com", "Microsoft API", "microsoft",
      "https://graph.microsoft.io/en-us/docs/get-started/rest",
      "https://apps.dev.microsoft.com/#/appList")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer $token").build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {

    val clientId = Secrets.prompt("Microsoft Client Id", "microsoft.clientId", "", false)
    val clientSecret = Secrets.prompt("Microsoft Client Secret", "microsoft.clientSecret", "", true)

    val scopes = Secrets.promptArray("Scopes", "microsoft.scopes", Arrays.asList(
      "User.Read", "Contacts.Read", "Calendars.Read", "Mail.Read", "email", "offline_access", "openid", "profile", "Files.ReadWrite"
    ))

    return MicrosoftAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  override fun canRenew(credentials: Oauth2Token): Boolean {
    return credentials.isRenewable()
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

    return Oauth2Token(responseMap.access_token, responseMap.refresh_token, credentials.clientId, credentials.clientSecret)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache,
                            tokenSet: com.baulsupp.oksocial.credentials.Token): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "itemId", {
      credentialsStore.get(serviceDefinition(), tokenSet)?.let {
        client.query<DriveRootList>("https://graph.microsoft.com/v1.0/me/drive/root/children", tokenSet).value.map { it.id }
      }
    })

    return completer
  }

  override suspend fun validate(client: OkHttpClient, credentials: Oauth2Token): ValidatedCredentials =
    ValidatedCredentials(client.query<User>("https://graph.microsoft.com/v1.0/me", TokenValue(credentials)).displayName)

  override fun hosts(): Set<String> = setOf("graph.microsoft.com")
}
