package com.baulsupp.oksocial.services.stackexchange

import com.baulsupp.oksocial.TokenValue
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.Arrays

class StackExchangeAuthInterceptor : AuthInterceptor<StackExchangeToken>() {
  override fun serviceDefinition(): StackExchangeServiceDefinition {
    return StackExchangeServiceDefinition()
  }

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
                                credentials: StackExchangeToken): ValidatedCredentials {
    val map = client.queryMap<Any>("https://api.stackexchange.com/2.2/me?site=drupal", TokenValue(credentials))
    return ValidatedCredentials(extract(map))
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): StackExchangeToken {

    val clientId = Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false)
    val clientSecret = Secrets.prompt("StackExchange Client Secret", "stackexchange.clientSecret",
      "", true)
    val clientKey = Secrets.prompt("StackExchange Key", "stackexchange.key", "", false)
    val scopes = Secrets.promptArray("Scopes", "stackexchange.scopes", Arrays.asList("read_inbox",
      "no_expiry",
      "write_access",
      "private_info"))

    return StackExchangeAuthFlow.login(client, outputHandler, clientId, clientSecret, clientKey,
      scopes)
  }

  override fun hosts(): Set<String> = setOf("api.stackexchange.com")
}
