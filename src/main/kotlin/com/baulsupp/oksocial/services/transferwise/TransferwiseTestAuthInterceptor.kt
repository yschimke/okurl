package com.baulsupp.oksocial.services.transferwise

import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.BaseUrlCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.credentials.CredentialsStore
import okhttp3.OkHttpClient
import java.io.IOException

class TransferwiseTestAuthInterceptor : TransferwiseAuthInterceptor() {
  override fun host(): String {
    return "test-restgw.transferwise.com"
  }

  override fun serviceDefinition(): Oauth2ServiceDefinition {
    return Oauth2ServiceDefinition(host(), "Transferwise Test API", "transferwise-test",
            "https://api-docs.transferwise.com/",
            "https://api-docs.transferwise.com/api-explorer/transferwise-api/versions/v1/")
  }

  @Throws(IOException::class)
  override fun apiCompleter(prefix: String, client: OkHttpClient,
                            credentialsStore: CredentialsStore,
                            completionVariableCache: CompletionVariableCache): ApiCompleter {
    val urlList = UrlList.fromResource("transferwise")!!

    val testUrls = urlList.getUrls("")
            .map { s -> s.replace("api.transferwise.com", host()) }

    return BaseUrlCompleter(UrlList(UrlList.Match.SITE, testUrls), hosts(), completionVariableCache)
  }
}
