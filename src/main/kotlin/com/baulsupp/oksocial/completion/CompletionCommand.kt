package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.Main
import java.io.File

class CompletionCommand(val main: Main) {
  suspend fun urlCompletionList(): String {
    val command = main.getShellCommand()

    val commandCompletor = command.completer()
    if (commandCompletor != null) {
      val urls = commandCompletion(commandCompletor, main.arguments)

      val prefix = main.arguments.last()

      if (main.completionFile != null) {
        urls.toFile(File(main.completionFile), 0, prefix)
      }

      return urls.getUrls(prefix).joinToString("\n")
    }

    val completer = UrlCompleter(main.serviceInterceptor!!.services(), main.client!!, main.credentialsStore!!,
            main.completionVariableCache!!)

    val fullCompletionUrl = main.getFullCompletionUrl()

    // reload hack (in case changed for "" case)
    val originalCompletionUrl = main.arguments.last()

    if (fullCompletionUrl != null) {
      val urls = completer.urlList(fullCompletionUrl)

      val strip: Int = if (fullCompletionUrl != originalCompletionUrl) {
        fullCompletionUrl.length - originalCompletionUrl.length
      } else {
        0
      }

      if (main.completionFile != null) {
        urls.toFile(File(main.completionFile), strip, originalCompletionUrl)
      }

      return urls.getUrls(fullCompletionUrl).joinToString("\n") { it.substring(strip) }
    } else {
      return ""
    }
  }

  suspend fun complete() {
    main.outputHandler!!.info(urlCompletionList())
  }

  suspend fun commandCompletion(urlCompleter: ArgumentCompleter, arguments: List<String>): UrlList {
    return urlCompleter.urlList(arguments[arguments.size - 1])
  }
}
