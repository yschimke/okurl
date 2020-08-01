package com.baulsupp.okurl.completion

import com.baulsupp.okurl.Main
import java.util.logging.Level
import java.util.logging.Logger

class CompletionCommand(val main: Main) {
  suspend fun urlCompletionList(): String {
    val completer = UrlCompleter(main)

    val fullCompletionUrl = main.getFullCompletionUrl()

    // reload hack (in case changed for "" case)
    val originalCompletionUrl = main.arguments.last()

    if (fullCompletionUrl != null) {
      val urls = completer.urlList(fullCompletionUrl, main.token())

      val strip: Int = if (fullCompletionUrl != originalCompletionUrl) {
        fullCompletionUrl.length - originalCompletionUrl.length
      } else {
        0
      }

      return urls.getUrls(fullCompletionUrl).joinToString("\n") { it.substring(strip) }
    } else {
      return ""
    }
  }

  suspend fun complete() {
    try {
      main.outputHandler.info(urlCompletionList())
    } catch (e: Exception) {
      logger.log(Level.FINE, "failure during url completion", e)
    }
  }

  companion object {
    private val logger = Logger.getLogger(CompletionCommand::class.java.name)
  }
}
