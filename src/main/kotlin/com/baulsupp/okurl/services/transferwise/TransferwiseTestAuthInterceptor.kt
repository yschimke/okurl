package com.baulsupp.okurl.services.transferwise

import com.baulsupp.okurl.authenticator.oauth2.Oauth2ServiceDefinition
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import okhttp3.OkHttpClient

class TransferwiseTestAuthInterceptor : BaseTransferwiseAuthInterceptor() {
  override val serviceDefinition = Oauth2ServiceDefinition(
    "test-restgw.transferwise.com", "Transferwise Test API", "transferwise-test",
    "https://api-docs.transferwise.com/",
    "https://api-docs.transferwise.com/api-explorer/transferwise-api/versions/v1/"
  )

  override suspend fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter {
    val urlList = UrlList.fromResource("transferwise")!!

    val testUrls = urlList.getUrls("")
      .map { s -> s.replace("api.transferwise.com", serviceDefinition.apiHost()) }

    return BaseUrlCompleter(UrlList(UrlList.Match.SITE, testUrls), hosts(credentialsStore), completionVariableCache)
  }
}
