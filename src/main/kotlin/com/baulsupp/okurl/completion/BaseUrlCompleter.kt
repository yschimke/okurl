package com.baulsupp.okurl.completion

import com.baulsupp.okurl.credentials.Token
import okhttp3.HttpUrl

class BaseUrlCompleter(
  private val urlList: UrlList,
  hosts: Collection<String>,
  private val completionVariableCache: CompletionVariableCache
) :
  HostUrlCompleter(hosts) {
  private val mappings = CompletionMappings()

  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    return mappings.replaceVariables(urlList)
  }

  fun withVariable(name: String, values: suspend () -> List<String>?) {
    mappings.withVariable(name) { values().orEmpty() }
  }

  fun withCachedVariable(name: String, field: String, fn: suspend () -> List<String>?) {
    withVariable(field) { completionVariableCache.compute(name, field, fn) }
  }
}
