package com.baulsupp.okurl.services.paypal

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import okhttp3.OkHttpClient

class PaypalSandboxAuthInterceptor : PaypalAuthInterceptor() {
  override fun shortName(): String = "paypal-sandbox"

  override fun host(): String = "api.sandbox.paypal.com"

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val testUrls = UrlList.fromResource("paypal")!!.getUrls("")
      .map { s -> s.replace("api.paypal.com", host()) }

    return BaseUrlCompleter(UrlList(UrlList.Match.SITE, testUrls), hosts(), completionVariableCache)
  }
}
