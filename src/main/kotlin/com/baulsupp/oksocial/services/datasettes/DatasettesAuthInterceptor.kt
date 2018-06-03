package com.baulsupp.oksocial.services.datasettes

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.services.datasettes.model.DatasetteIndex2
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
