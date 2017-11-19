package com.baulsupp.oksocial.services.squareup

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionQuery
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

class SquareUpAuthInterceptor : AuthInterceptor<Oauth2Token> {
  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition("connect.squareup.com", "SquareUp API", "squareup",
        "https://docs.connect.squareup.com/api/connect/v2/", "https://connect.squareup.com/apps")
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: Oauth2Token): Response {
    var request = chain.request()

    val token = credentials.accessToken

    val reqBuilder = request.newBuilder().addHeader("Authorization", "Bearer " + token)
    if (request.header("Accept") == null) {
      reqBuilder.addHeader("Accept", "application/json")
    }
    request = reqBuilder.build()

    return chain.proceed(request)
  }

  @Throws(IOException::class)
  override fun validate(client: OkHttpClient,
                        requestBuilder: Request.Builder, credentials: Oauth2Token): Future<ValidatedCredentials> {
    return JsonCredentialsValidator(
        SquareUpUtil.apiRequest("/v1/me", requestBuilder), { it["name"] as String }).validate(
        client)
  }

  @Throws(IOException::class)
  override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                         authArguments: List<String>): Oauth2Token {
    System.err.println("Authorising SquareUp API")

    val clientId = Secrets.prompt("SquareUp Application Id", "squareup.clientId", "", false)
    val clientSecret = Secrets.prompt("SquareUp Application Secret", "squareup.clientSecret", "", true)
    val scopes = Secrets.promptArray("Scopes", "squareup.scopes", SquareUpUtil.ALL_PERMISSIONS)

    return SquareUpAuthFlow.login(client, outputHandler, clientId, clientSecret, scopes)
  }

  @Throws(IOException::class)
  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val credentials = credentialsStore.readDefaultCredentials(serviceDefinition())

    val completer = BaseUrlCompleter(urlList!!, hosts())

    credentials?.let {
      completer.withVariable("location",
          {
            completionVariableCache.compute(name(), "locations",
                {
                  CompletionQuery.getIds(client, "https://connect.squareup.com/v2/locations",
                      "locations",
                      "id")
                })
          })
    }

    return completer
  }

  override fun hosts(): Set<String> {
    return SquareUpUtil.API_HOSTS
  }
}
