package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionMappings
import com.baulsupp.oksocial.completion.UrlList
import com.baulsupp.oksocial.util.ClientException
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import okhttp3.HttpUrl
import java.util.logging.Level
import java.util.logging.Logger

class GoogleDiscoveryCompleter(private val discoveryRegistry: DiscoveryRegistry,
                               private val discoveryDocPaths: List<String>) : ApiCompleter {
  private val mappings = CompletionMappings()

  init {
    mappings.withVariable("userId", { listOf("me") })
  }

  override suspend fun prefixUrls(): UrlList {
    throw UnsupportedOperationException()
  }

  override suspend fun siteUrls(url: HttpUrl, tokenSet: Token): UrlList {
    val futures = discoveryDocPaths.map {
      async(CommonPool) {
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

    return mappings.replaceVariables(UrlList(UrlList.Match.SITE, futures))
  }

  companion object {
    private val logger = Logger.getLogger(GoogleDiscoveryCompleter::class.java.name)

    fun forApis(discoveryRegistry: DiscoveryRegistry,
                discoveryDocPaths: List<String>): GoogleDiscoveryCompleter {
      return GoogleDiscoveryCompleter(discoveryRegistry, discoveryDocPaths)
    }
  }
}
