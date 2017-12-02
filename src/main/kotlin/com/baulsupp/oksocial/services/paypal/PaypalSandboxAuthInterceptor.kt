package com.baulsupp.oksocial.services.paypal

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import okhttp3.OkHttpClient

class PaypalSandboxAuthInterceptor : PaypalAuthInterceptor() {
  override fun shortName(): String = "paypal-sandbox"

  override fun host(): String = "api.sandbox.paypal.com"

  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    val testUrls = UrlList.fromResource("paypal")!!.getUrls("")
            .map { s -> s.replace("api.paypal.com", host()) }

    return BaseUrlCompleter(UrlList(UrlList.Match.SITE, testUrls), hosts(), completionVariableCache)
  }
}
