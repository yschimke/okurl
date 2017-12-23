package com.baulsupp.oksocial.services.gdax

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.queryList
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.gdax.model.Account
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class GdaxAuthInterceptor : AuthInterceptor<GdaxCredentials>() {
  override fun serviceDefinition() = GdaxAuthServiceDefinition()

  override fun intercept(chain: Interceptor.Chain, credentials: GdaxCredentials): Response {
    var request = chain.request()

    val timestamp = System.currentTimeMillis() / 1000;

    val signature = ""

    request = request.newBuilder()
            .addHeader("CB-ACCESS-KEY", credentials.apiKey)
            .addHeader("CB-ACCESS-SIGN", signature)
            .addHeader("CB-ACCESS-TIMESTAMP", timestamp.toString())
            .addHeader("CB-ACCESS-PASSPHRASE", credentials.passphrase)
            .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): GdaxCredentials {
    val apiKey = Secrets.prompt("GDAX API Key", "gdax.apiKey", "", false)
    val apiSecret = Secrets.prompt("GDAX API Secret", "gdax.apiSecret", "", true)
    val apiPassphrase = Secrets.prompt("GDAX Passphrase", "gdax.passphrase", "", true)

    return GdaxCredentials(apiKey, apiSecret, apiPassphrase)
  }

  override suspend fun validate(client: OkHttpClient,
                                credentials: GdaxCredentials): ValidatedCredentials {
    val accounts = client.queryList<Account>("https://api.gdax.com/accounts")
    return ValidatedCredentials(accounts.map { it.id }.first())
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

//    completer.withVariable("AccountSid", {
//      credentialsStore[serviceDefinition()]?.let { listOf(it.user) }
//    })

    return completer
  }

  override fun hosts(): Set<String> = setOf("api.gdax.com")
}
