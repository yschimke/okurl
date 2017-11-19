package com.baulsupp.oksocial.services.datasettes

import com.baulsupp.oksocial.apidocs.ApiDocPresenter
import com.baulsupp.oksocial.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * https://datasettes.com/
 */
class DatasettesAuthInterceptor : CompletionOnlyAuthInterceptor("datasettes.com", "Datasettes", "datasettes", "https://github.com/simonw/datasette") {
  override fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter =
      DatasettesCompleter()

  override fun hosts(): Set<String> = knownHosts()

  override fun apiDocPresenter(url: String): ApiDocPresenter = DatasettesPresenter()
}

class DatasettesCompleter : HostUrlCompleter(knownHosts()) {
}

class DatasettesPresenter: ApiDocPresenter {
  override fun explainApi(url: String, outputHandler: OutputHandler<Response>, client: OkHttpClient) {
    val urlI = HttpUrl.parse(url)

    outputHandler.info("service: Datasette")
    outputHandler.info("name: ${urlI?.host() ?: "unknown"}")
    outputHandler.info("docs: https://github.com/simonw/datasette")
  }
}

fun knownHosts(): Set<String> {
  return setOf("fivethirtyeight.datasettes.com", "parlgov.datasettes.com")
}
