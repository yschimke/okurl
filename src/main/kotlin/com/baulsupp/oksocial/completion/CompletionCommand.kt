package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.Main
import java.util.logging.Level
import java.util.logging.Logger

class CompletionCommand(val main: Main) {
  suspend fun urlCompletionList(): String {
    val command = main.getShellCommand()

    val commandCompletor = command.completer()
    if (commandCompletor != null) {
      val urls = commandCompletion(commandCompletor, main.arguments)

      val prefix = main.arguments.last()

      if (main.completionFile != null) {
        urls.toFile(main.completionFile!!, 0, prefix)
      }

      return urls.getUrls(prefix).joinToString("\n")
    }

    val completer = UrlCompleter(main.serviceInterceptor.services(), main.client, main.credentialsStore,
      main.completionVariableCache)

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

      main.completionFile?.let {
        it.delete()

        if (urls.match == UrlList.Match.HOSTS && urls.urls.isEmpty()) {
          main.outputHandler.showError("Unable to complete hosts")
        }

        urls.toFile(it, strip, originalCompletionUrl)
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

  suspend fun commandCompletion(urlCompleter: ArgumentCompleter, arguments: List<String>): UrlList {
    return urlCompleter.urlList(arguments[arguments.size - 1], main.token())
  }

  companion object {
    private val logger = Logger.getLogger(CompletionCommand::class.java.name)
  }
}
