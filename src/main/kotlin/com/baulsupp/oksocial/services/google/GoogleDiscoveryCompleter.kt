package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionMappings
import com.baulsupp.oksocial.completion.UrlList
import com.google.common.collect.Lists
import com.spotify.futures.CompletableFutures
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import okhttp3.HttpUrl
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

class GoogleDiscoveryCompleter(private val discoveryRegistry: DiscoveryRegistry,
                               private val discoveryDocPaths: List<String>) : ApiCompleter {
  private val mappings = CompletionMappings()

  init {
    mappings.withVariable("userId", Lists.newArrayList("me"))
  }

  @Throws(IOException::class)
  override fun prefixUrls(): CompletableFuture<UrlList> {
    // not supported for partial urls
    throw UnsupportedOperationException()
  }

  @Throws(IOException::class)
  override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
    val futures = discoveryDocPaths.map { this.singleFuture(it) }

    return CompletableFutures.allAsList(futures).map { flattenList(it) }.flatMap { mappings.replaceVariables(it) }
  }

  private fun flattenList(l: List<List<String>>): UrlList {
    return UrlList(UrlList.Match.SITE, l.flatMap { it })
  }

  private fun singleFuture(discoveryDocPath: String): CompletableFuture<List<String>> {
    return discoveryRegistry.load(discoveryDocPath).thenApply { s -> s.urls }.exceptionally { t ->
      logger.log(Level.FINE, "failed request for " + discoveryDocPath, t)
      Lists.newArrayList()
    }
  }

  companion object {
    private val logger = Logger.getLogger(GoogleDiscoveryCompleter::class.java.name)

    fun forApis(discoveryRegistry: DiscoveryRegistry,
                discoveryDocPaths: List<String>): GoogleDiscoveryCompleter {
      return GoogleDiscoveryCompleter(discoveryRegistry, discoveryDocPaths)
    }
  }
}
