package com.baulsupp.okurl.services.hitbtc

import com.baulsupp.oksocial.output.handler.OutputHandler
import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.BasicCredentials
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.credentials.TokenValue
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.okurl.secrets.Secrets
import com.baulsupp.okurl.services.hitbtc.model.Currency
import com.baulsupp.okurl.services.hitbtc.model.Symbol
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class HitBTCAuthInterceptor : AuthInterceptor<BasicCredentials>() {
  override val serviceDefinition = BasicAuthServiceDefinition(
    "api.hitbtc.com", "HitBTC API", "hitbtc",
    "https://api.hitbtc.com/", "https://hitbtc.com/settings/api-keys"
  )

  override suspend fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): BasicCredentials {
    val user = Secrets.prompt("BitHTC API Key", "bithtc.apiKey", "", false)
    val password = Secrets.prompt("BitHTC Secret Key", "bithtc.secretKey", "", true)

    return BasicCredentials(user, password)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: BasicCredentials
  ): ValidatedCredentials {
    client.queryList<Any>(
      "https://api.hitbtc.com/api/2/account/balance",
      TokenValue(credentials)
    )
    return ValidatedCredentials()
  }

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(credentialsStore), completionVariableCache)

    completer.withVariable("currency") {
      client.queryList<Currency>("https://api.hitbtc.com/api/2/public/currency", tokenSet).map { it.id }
    }
    completer.withVariable("symbol") {
      client.queryList<Symbol>("https://api.hitbtc.com/api/2/public/symbol", tokenSet).map { it.id }
    }

    return completer
  }
}
