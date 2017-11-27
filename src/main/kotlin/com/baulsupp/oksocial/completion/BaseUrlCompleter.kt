package com.baulsupp.oksocial.completion

import okhttp3.HttpUrl

class BaseUrlCompleter(private val urlList: UrlList, hosts: Collection<String>) : HostUrlCompleter(hosts) {
  private val mappings = CompletionMappings()

  override suspend fun siteUrls(url: HttpUrl): UrlList {
    return mappings.replaceVariables(urlList)
  }

  fun withVariable(name: String, values: suspend () -> List<String>) {
    mappings.withVariable(name, values)
  }
}
