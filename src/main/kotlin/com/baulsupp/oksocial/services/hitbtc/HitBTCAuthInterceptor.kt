package com.baulsupp.oksocial.services.hitbtc

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.kotlin.queryList
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.secrets.Secrets
import com.baulsupp.oksocial.services.hitbtc.model.Currency
import com.baulsupp.oksocial.services.hitbtc.model.Symbol
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class HitBTCAuthInterceptor : AuthInterceptor<BasicCredentials>() {
  override fun serviceDefinition(): BasicAuthServiceDefinition {
    return BasicAuthServiceDefinition("api.hitbtc.com", "HitBTC API", "hitbtc",
            "https://api.hitbtc.com/", "https://hitbtc.com/settings/api-keys")
  }

  override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
            .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
            .build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>,
                                 authArguments: List<String>): BasicCredentials {
    val user = Secrets.prompt("BitHTC API Key", "bithtc.apiKey", "", false)
    val password = Secrets.prompt("BitHTC Secret Key", "bithtc.secretKey", "", true)

    return BasicCredentials(user, password)
  }

  suspend override fun validate(client: OkHttpClient,
                                credentials: BasicCredentials): ValidatedCredentials {
    val account = client.queryList<Any>("https://api.hitbtc.com/api/2/account/balance").let { "✓" }
    return ValidatedCredentials(account)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withVariable("currency", {
      client.queryList<Currency>("https://api.hitbtc.com/api/2/public/currency").map { it.id }
    })
    completer.withVariable("symbol", {
      client.queryList<Symbol>("https://api.hitbtc.com/api/2/public/symbol").map { it.id }
    })

    return completer
  }

  override fun hosts(): Set<String> = setOf("api.hitbtc.com")
}
