package com.baulsupp.oksocial.services.coinbin

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.authenticator.BasicCredentials
import com.baulsupp.oksocial.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.ServiceDefinition
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://coinbin.org/
 */
class CoinBinAuthInterceptor : AuthInterceptor<BasicCredentials>() {

  override fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  suspend override fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): BasicCredentials {
    throw UnsupportedOperationException()
  }

  override fun serviceDefinition(): ServiceDefinition<BasicCredentials> =
    BasicAuthServiceDefinition("coinbin.org", "Coin Bin", "coinbin",
      "https://coinbin.org/", null)

  suspend override fun validate(client: OkHttpClient,
                                credentials: BasicCredentials): ValidatedCredentials =
    ValidatedCredentials(credentials.user, null)

  override fun hosts(): Set<String> = setOf("coinbin.org")

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache,
                            tokenSet: String?): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "coin", {
      client.query<Coins>("https://coinbin.org/coins").coins.keys.toList()
    })

    return completer
  }
}
