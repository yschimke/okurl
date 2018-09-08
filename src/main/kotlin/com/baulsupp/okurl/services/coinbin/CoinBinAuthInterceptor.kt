package com.baulsupp.okurl.services.coinbin

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
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.UsageException
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://coinbin.org/
 */
class CoinBinAuthInterceptor : AuthInterceptor<BasicCredentials>() {

  override suspend fun intercept(chain: Interceptor.Chain, credentials: BasicCredentials): Response {
    var request = chain.request()

    request = request.newBuilder()
      .addHeader("Authorization", Credentials.basic(credentials.user, credentials.password))
      .build()

    return chain.proceed(request)
  }

  override suspend fun authorize(client: OkHttpClient, outputHandler: OutputHandler<Response>, authArguments: List<String>): BasicCredentials {
    throw UsageException("authorization not required")
  }

  override val serviceDefinition =
    BasicAuthServiceDefinition("coinbin.org", "Coin Bin", "coinbin",
      "https://coinbin.org/", null)

  override suspend fun validate(
    client: OkHttpClient,
    credentials: BasicCredentials
  ): ValidatedCredentials =
    ValidatedCredentials(credentials.user, null)

  override fun hosts(): Set<String> = setOf("coinbin.org")

  override fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "coin", {
      client.query<Coins>("https://coinbin.org/coins", tokenSet).coins.keys.toList()
    })

    return completer
  }
}
