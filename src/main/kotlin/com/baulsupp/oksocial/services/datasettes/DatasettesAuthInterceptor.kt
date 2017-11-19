package com.baulsupp.oksocial.services.datasettes

import com.baulsupp.oksocial.authenticator.CompletionOnlyAuthInterceptor
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionVariableCache
import com.baulsupp.oksocial.completion.HostUrlCompleter
import com.baulsupp.oksocial.credentials.CredentialsStore
import okhttp3.OkHttpClient

/**
 * https://datasettes.com/
 */
class DatasettesAuthInterceptor : CompletionOnlyAuthInterceptor("datasettes.com", "Datasettes", "datasettes", "https://github.com/simonw/datasette") {
  override fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache): ApiCompleter {
    return DatasettesCompleter()
  }

  override fun hosts(): Collection<String> {
    return knownHosts()
  }
}

class DatasettesCompleter : HostUrlCompleter(knownHosts()) {

}

fun knownHosts(): Iterable<String> {
  return listOf("fivethirtyeight.datasettes.com", "parlgov.datasettes.com")
}
