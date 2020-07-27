package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.CompletionMappings
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.completion.UrlList.Match.SITE
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.HttpUrl
import java.util.logging.Level
import java.util.logging.Logger

class GoogleDiscoveryCompleter(
  private val discoveryRegistry: DiscoveryRegistry,
  private val discoveryDocPaths: List<String>
) : ApiCompleter {
  private val mappings = CompletionMappings()

  init {
    mappings.withVariable("userId") { listOf("me") }
  }

  override suspend fun prefixUrls(): UrlList {
    throw UnsupportedOperationException()
  }

  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList = supervisorScope {
    val futures = discoveryDocPaths.map {
      async {
        discoveryRegistry.load(it, tokenSet).urls
      }
    }.mapNotNull {
      try {
        it.await()
      } catch (e: ClientException) {
        logger.log(Level.FINE, "failed getting siteUrls for $url", e)
        null
      } catch (e: CancellationException) {
        logger.log(Level.FINE, "timeout for $url", e)
        null
      }
    }.flatten()

    mappings.replaceVariables(UrlList(SITE, futures))
  }

  companion object {
    private val logger = Logger.getLogger(GoogleDiscoveryCompleter::class.java.name)

    fun forApis(
      discoveryRegistry: DiscoveryRegistry,
      discoveryDocPaths: List<String>
    ): GoogleDiscoveryCompleter {
      return GoogleDiscoveryCompleter(discoveryRegistry, discoveryDocPaths)
    }
  }
}
