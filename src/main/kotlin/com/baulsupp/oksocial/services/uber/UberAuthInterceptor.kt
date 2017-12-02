package com.baulsupp.oksocial.services.uber

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UberAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition(host(), "Uber API", "uber",
            "https://developer.uber.com/docs/riders/references/api",
            "https://developer.uber.com/dashboard/")
  }

  private fun host(): String {
    return "api.uber.com"
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    request = request.newBuilder().addHeader("Authorization", "Bearer " + token).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising Uber API")

    val clientId = Secrets.prompt("Uber Client Id", "uber.clientId", "", false)
    val clientSecret = Secrets.prompt("Uber Client Secret", "uber.clientSecret", "", true)

    return UberAuthFlow.login(client, outputHandler, clientId, clientSecret)
  }

  @Throws(IOException::class)
  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    return BaseUrlCompleter(UrlList.fromResource(name())!!, hosts(), completionVariableCache)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: Oauth2Token): ValidatedCredentials {
    return JsonCredentialsValidator(
            Request.Builder().url("https://api.uber.com/v1/me").build(),
            { map -> "${map["first_name"]} ${map["last_name"]}" }).validate(client)
  }

  override fun hosts(): Set<String> {
    return setOf(
            "api.uber.com", "login.uber.com", "sandbox-api.uber.com")

  }

  override fun canRenew(credentials: Oauth2Token): Boolean {
    return credentials.isRenewable()
  }

  override suspend fun renew(client: OkHttpClient, credentials: Oauth2Token): Oauth2Token? {
    val tokenUrl = "https://login.uber.com/oauth/v2/token"

    val body = FormBody.Builder().add("client_id", credentials.clientId!!)
            .add("client_secret", credentials.clientSecret!!)
            .add("refresh_token", credentials.refreshToken!!)
            .add("grant_type", "refresh_token")
            .build()

    val request = Request.Builder().url(tokenUrl).method("POST", body).build()

    val responseMap = AuthUtil.makeJsonMapRequest(client, request)

    return Oauth2Token(responseMap["access_token"] as String,
            responseMap["refresh_token"] as String, credentials.clientId,
            credentials.clientSecret)
  }
}
