package com.baulsupp.okurl.services.stackexchange

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.stackexchange.model.MeResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class StackExchangeAuthInterceptor : AuthInterceptor<StackExchangeToken>() {
  override val serviceDefinition = StackExchangeServiceDefinition()

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

  override suspend fun validate(client: OkHttpClient, credentials: StackExchangeToken): ValidatedCredentials {
    return client.query<MeResponse>("https://api.stackexchange.com/2.2/me?site=stackoverflow", TokenValue(credentials)).let { ValidatedCredentials(it.items.firstOrNull()?.display_name) }
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): StackExchangeToken {

    val clientId = Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false)
    val clientSecret = Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret",
      "", true)
    val clientKey = Secrets.prompt("StackExchange Key", "stackexchange.key", "", false)
    val scopes = Secrets.promptArray("Scopes", "stackexchange.scopes", listOf("read_inbox",
      "no_expiry",
      "write_access",
      "private_info"))

    return StackExchangeAuthFlow.login(client, outputHandler, clientId, clientSecret, clientKey, scopes)
  }

  override fun hosts(): Set<String> = setOf("api.stackexchange.com")
}
