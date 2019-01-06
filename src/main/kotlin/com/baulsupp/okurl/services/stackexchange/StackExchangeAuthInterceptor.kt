package com.baulsupp.okurl.services.stackexchange

import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.credentials.ServiceDefinition
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.stackexchange.model.MeResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class StackExchangeAuthInterceptor : AuthInterceptor<StackExchangeToken>() {
  override val serviceDefinition = object : ServiceDefinition<StackExchangeToken> {
    override fun apiHost() = "api.stackexchange.com"

    override fun serviceName() = "StackExchange API"

    override fun shortName() = "stackexchange"

    override fun parseCredentialsString(s: String): StackExchangeToken {
      val parts = s.split(":".toRegex(), 2).toTypedArray()
      return StackExchangeToken(parts[0], parts[1])
    }

    override fun formatCredentialsString(credentials: StackExchangeToken) =
      credentials.accessToken + ":" + credentials.key

    override fun apiDocs() = "https://api.stackexchange.com/docs"

    override fun accountsLink() = "http://stackapps.com/apps/oauth"
  }

  override suspend fun intercept(chain: Interceptor.Chain, credentials: StackExchangeToken): Response {
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
    return ValidatedCredentials(
      client.query<MeResponse>(
        "https://api.stackexchange.com/2.2/me?site=stackoverflow",
        TokenValue(credentials)
      ).items.firstOrNull()?.display_name
    )
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): StackExchangeToken {

    val clientId = Secrets.prompt("StackExchange Client Id", "stackexchange.clientId", "", false)
    val clientSecret = Secrets.prompt(
      "StackExchange Client Secret", "stackexchange.clientSecret",
      "", true
    )
    val clientKey = Secrets.prompt("StackExchange Key", "stackexchange.key", "", false)
    val scopes = Secrets.promptArray(
      "Scopes", "stackexchange.scopes", listOf(
        "read_inbox",
        "no_expiry",
        "write_access",
        "private_info"
      )
    )

    return StackExchangeAuthFlow.login(client, outputHandler, clientId, clientSecret, clientKey, scopes)
  }
}
