package com.baulsupp.okurl.completion

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl
import java.lang.Math.min
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class UrlCompleter(val main: Main) : ArgumentCompleter {
  override suspend fun urlList(prefix: String, tokenSet: Token): UrlList {
    val fullUrl = parseUrl(prefix)

    return if (fullUrl != null) {
      pathCompletion(fullUrl, prefix, tokenSet)
    } else {
      hostCompletion(tokenSet)
    }
  }

  private suspend fun pathCompletion(fullUrl: HttpUrl, prefix: String, tokenSet: Token): UrlList {
    return (main.authenticatingInterceptor.services
      .firstOrNull { it.supportsUrl(fullUrl) }
      ?.apiCompleter(prefix, client, main.credentialsStore, main.completionVariableCache, tokenSet)
      ?.siteUrls(fullUrl, tokenSet)
      ?: UrlList(UrlList.Match.EXACT, listOf()))
  }

  private suspend fun UrlCompleter.hostCompletion(tokenSet: Token): UrlList {
    val futures = main.authenticatingInterceptor.services.map {
      async(CommonPool) {
        withTimeout(2, TimeUnit.SECONDS) {
          it.apiCompleter("", client, main.credentialsStore, main.completionVariableCache, tokenSet).prefixUrls()
        }
      }
    }

    val results = mutableListOf<String>()
    for (f in futures) {
      try {
        results.addAll(f.await().getUrls(""))
      } catch (e: ClientException) {
        logger.log(Level.WARNING, "http error during url completion", e)
      } catch (e: CancellationException) {
        logger.log(Level.WARNING, "failure during url completion", e.cause)
      } catch (e: ExecutionException) {
        logger.log(Level.WARNING, "failure during url completion", e.cause)
      }
    }
    return UrlList(UrlList.Match.HOSTS, results)
  }

  private fun parseUrl(prefix: String): HttpUrl? = if (isSingleApi(prefix)) {
    HttpUrl.parse(prefix)
  } else {
    null
  }

  private fun isSingleApi(prefix: String): Boolean = prefix.matches("https://[^/]+/.*".toRegex())

  companion object {
    private val logger = Logger.getLogger(UrlCompleter::class.java.name)

    fun isPossibleAddress(urlCompletion: String): Boolean {
      return urlCompletion.startsWith("https://".substring(0, min(urlCompletion.length, 8)))
    }
  }
}
