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

  fun withVariable(name: String, keepTemplate: Boolean = true, values: suspend () -> List<String>?) {
    mappings.withVariable(name, keepTemplate = keepTemplate) { values().orEmpty() }
  }

  fun withCachedVariable(name: String, field: String, keepTemplate: Boolean = true, fn: suspend () -> List<String>?) {
    withVariable(field, keepTemplate) { completionVariableCache.compute(name, field, fn) }
  }
}
