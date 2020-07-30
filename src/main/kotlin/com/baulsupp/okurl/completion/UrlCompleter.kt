package com.baulsupp.okurl.completion

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.completion.UrlList.Match.HOSTS
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.util.ClientException
import com.baulsupp.okurl.util.FileUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import java.lang.Math.min
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit.SECONDS
import java.util.logging.Level
import java.util.logging.Logger

class UrlCompleter(val main: Main) : ArgumentCompleter {
  override suspend fun urlList(
    prefix: String,
    tokenSet: Token
  ): UrlList {
    val fullUrl = parseUrl(prefix)

    return if (fullUrl != null) {
      pathCompletion(fullUrl, prefix, tokenSet)
    } else {
      hostCompletion(tokenSet)
    }
  }

  private suspend fun pathCompletion(
    fullUrl: HttpUrl,
    prefix: String,
    tokenSet: Token
  ): UrlList {
    val authInterceptors = main.authenticatingInterceptor.services
      .filter { it.supportsUrl(fullUrl, main.credentialsStore) }

    val client = Main.client.newBuilder()
      .cache(Cache(File(FileUtil.okurlSettingsDir, "completion-cache"), 256 * 1024 * 1024))
      .build()

    authInterceptors.forEach {
      val apiCompleter = it.apiCompleter(
        prefix, client, main.credentialsStore, main.completionVariableCache, tokenSet
      )
      val results = apiCompleter.siteUrls(fullUrl, tokenSet)

      if (results.urls.isNotEmpty())
        return results
    }

    return UrlList.None
  }

  private suspend fun hostCompletion(tokenSet: Token): UrlList = supervisorScope {
    val futures = main.authenticatingInterceptor.services.map {
      async {
        withTimeout(SECONDS.toMillis(2)) {
          it.apiCompleter(
            "", Main.client, main.credentialsStore, main.completionVariableCache, tokenSet
          )
            .prefixUrls()
        }
      }
    }

    val results = mutableListOf<String>()
    for (f in futures) {
      try {
        results.addAll(
          f.await()
            .getUrls("")
        )
      } catch (e: ClientException) {
        logger.log(Level.WARNING, "http error during url completion", e)
      } catch (e: CancellationException) {
        logger.log(Level.WARNING, "failure during url completion", e.cause)
      } catch (e: ExecutionException) {
        logger.log(Level.WARNING, "failure during url completion", e.cause)
      }
    }

    UrlList(HOSTS, results)
  }

  private fun parseUrl(prefix: String): HttpUrl? = if (isSingleApi(prefix)) {
    prefix.toHttpUrlOrNull()
  } else {
    null
  }

  private fun isSingleApi(prefix: String): Boolean = prefix.matches("https://[^/]+/.*".toRegex())

  companion object {
    val NullCompleter = object : ApiCompleter {
      override suspend fun prefixUrls() = UrlList.None

      override suspend fun siteUrls(
        url: HttpUrl,
        tokenSet: Token
      ) = UrlList.None
    }

    private val logger = Logger.getLogger(UrlCompleter::class.java.name)

    fun isPossibleAddress(urlCompletion: String): Boolean {
      return urlCompletion.startsWith("https://".substring(0, min(urlCompletion.length, 8)))
    }
  }
}
