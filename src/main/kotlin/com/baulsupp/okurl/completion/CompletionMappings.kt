package com.baulsupp.okurl.completion

class CompletionMappings {
  private val mappings = mutableListOf<suspend (UrlList) -> UrlList>()

  fun withVariable(name: String, values: suspend () -> List<String>) {
    mappings.add { ul: UrlList -> ul.replace(name, values(), true) }
  }

  suspend fun replaceVariables(urlList: UrlList): UrlList {
    var list = urlList

    for (s in mappings) {
      list = s(list)
    }

    return list
  }
}
