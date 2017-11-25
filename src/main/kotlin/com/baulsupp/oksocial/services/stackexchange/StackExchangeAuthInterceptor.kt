package com.baulsupp.oksocial.services.stackexchange

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.stackexchange.StackExchangeUtil.apiRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

class StackExchangeAuthInterceptor : AuthInterceptor<StackExchangeToken> {
  override fun serviceDefinition(): StackExchangeServiceDefinition {
    return StackExchangeServiceDefinition()
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain, credentials: StackExchangeToken): Response {
    var request = chain.request()

    val newUrl = request.url()
        .newBuilder()
        .addQueryParameter("access_token", credentials.accessToken)
        .addQueryParameter("key", credentials.key)
        .build()

    request = request.newBuilder().url(newUrl).build()

    return chain.proceed(request)
  }

  private fun extract(map: Map<String, Any>): String {
    val items = map["items"] as List<Map<String, Any>>

    return if (items.isNotEmpty()) {
      "" + items[0]["display_name"]
    } else {
      "Unknown"
    }
  }

  override suspend fun validate(client: OkHttpClient,
                        requestBuilder: Request.Builder, credentials: StackExchangeToken): Future<ValidatedCredentials> {
    return JsonCredentialsValidator(apiRequest("/2.2/me?site=drupal", requestBuilder),
        this::extract).validate(client)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<*>,
                         authArguments: List<String>): StackExchangeToken {
    System.err.println("Authorising StackExchange API")

    val clientId = Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false)
    val clientSecret = Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret", "", true)
    val clientKey = Secrets.prompt("StackExchange Key", "stackexchange.key", "", false)
    val scopes = Secrets.promptArray("Scopes", "stackexchange.scopes", StackExchangeUtil.SCOPES)

    return StackExchangeAuthFlow.login(client, outputHandler, clientId, clientSecret, clientKey,
        scopes)
  }

  override fun hosts(): Set<String> {
    return StackExchangeUtil.API_HOSTS
  }
}
