package com.baulsupp.okurl.completion

import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class CompletionMappings {
  private val mappings = mutableListOf<suspend (UrlList) -> UrlList>()

  fun withVariable(name: String, keepTemplate: Boolean = true, values: suspend () -> List<String>) {
    mappings.add { ul: UrlList -> ul.replace(name, values(), keepTemplate) }
  }

  suspend fun replaceVariables(urlList: UrlList): UrlList {
    var list = urlList

    for (s in mappings) {
      try {
        list = s(list)
      } catch (e: IOException) {
        logger.log(Level.FINE, "completion mapping failed", e)
      }
    }

    return list
  }

  companion object {
    private val logger = Logger.getLogger(CompletionMappings::class.java.name)
  }
}
