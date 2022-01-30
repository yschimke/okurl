package com.baulsupp.okurl.authenticator.authflow

import com.baulsupp.schoutput.UsageException
import com.baulsupp.okurl.secrets.Secrets
import okhttp3.OkHttpClient

object PromptDesktopFlow {
  suspend fun <T> prompt(
    authFlow: PromptAuthFlow<T>,
    client: OkHttpClient
  ): T {
    authFlow.init(client)

    val options = authFlow.options()

    val params = options.map {
      val value = when (it) {
        is Prompt -> Secrets.prompt(it.label, it.param, it.default ?: "", it.secret)
        else -> throw UsageException("Unknown option $it")
      }

      it.param to value
    }.toMap()

    return authFlow.complete(params)
  }
}
