package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.credentials.CredentialsStore
import com.baulsupp.oksocial.util.ClientException
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.lang.Math.min
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class UrlCompleter(private val services: List<AuthInterceptor<*>>, private val client: OkHttpClient,
                   private val credentialsStore: CredentialsStore,
                   private val completionVariableCache: CompletionVariableCache) : ArgumentCompleter {
  suspend override fun urlList(prefix: String): UrlList {
    val fullUrl = parseUrl(prefix)

    return if (fullUrl != null) {
      pathCompletion(fullUrl, prefix)
    } else {
      hostCompletion(prefix)
    }
  }

  private suspend fun pathCompletion(fullUrl: HttpUrl, prefix: String): UrlList {
    return (services
            .firstOrNull { it.supportsUrl(fullUrl) }
            ?.apiCompleter(prefix, client, credentialsStore, completionVariableCache)
            ?.siteUrls(fullUrl)
            ?: UrlList(UrlList.Match.EXACT, listOf()))
  }

  private suspend fun UrlCompleter.hostCompletion(prefix: String): UrlList {
    val futures = services.map {
      async(CommonPool) {
        withTimeout(2, TimeUnit.SECONDS) {
          it.apiCompleter("", client, credentialsStore, completionVariableCache).prefixUrls()
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
