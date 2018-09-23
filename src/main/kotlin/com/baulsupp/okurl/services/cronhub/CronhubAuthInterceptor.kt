package com.baulsupp.okurl.services.cronhub

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class CronhubAuthInterceptor : AuthInterceptor<CronhubCredentials>() {
  override val serviceDefinition = CronhubAuthServiceDefinition

  override suspend fun intercept(chain: Interceptor.Chain, credentials: CronhubCredentials): Response {
    var request = chain.request()

    request = request.newBuilder().addHeader("X-Api-Key", credentials.token).build()

    return chain.proceed(request)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): CronhubCredentials {
    outputHandler.openLink("https://cronhub.io/settings/api")

    val apiKey = Secrets.prompt("Cronhub Public API Token", "cronhub.token", "", false)

    return CronhubCredentials(apiKey)
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: CronhubCredentials
  ): ValidatedCredentials = ValidatedCredentials()

  override fun canRenew(credentials: CronhubCredentials): Boolean = false

  override fun hosts(): Set<String> = setOf("cronhub.io")

  override suspend fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache, tokenSet: Token): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "uuid") {
      client.query<MonitorsResponse>("https://cronhub.io/api/v1/monitors", tokenSet).response.map { it.code }
    }

    return completer
  }
}
