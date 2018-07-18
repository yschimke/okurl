package com.baulsupp.okurl.services.datasettes

import com.baulsupp.okurl.apidocs.ApiDocPresenter
import com.baulsupp.okurl.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import okhttp3.OkHttpClient

/**
 * https://datasettes.com/
 */
class DatasettesAuthInterceptor :
  CompletionOnlyAuthInterceptor("datasettes.com", "Datasettes", "datasettes",
    "https://github.com/simonw/datasette") {
  override fun apiCompleter(
    prefix: String,
    client: OkHttpClient,
    credentialsStore: CredentialsStore,
    completionVariableCache: CompletionVariableCache,
    tokenSet: Token
  ): ApiCompleter =
    DatasettesCompleter(client)

  override fun hosts(): Set<String> = knownHosts()

  override fun apiDocPresenter(url: String, client: OkHttpClient): ApiDocPresenter = DatasettesPresenter()
}

fun knownHosts(): Set<String> =
  DatasettesAuthInterceptor::class.java.getResource("/datasettes.txt")?.readText()?.split('\n')?.toSet() ?: setOf()
