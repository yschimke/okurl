package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.completion.ApiCompleter
import com.baulsupp.oksocial.completion.CompletionMappings
import com.baulsupp.oksocial.completion.UrlList
import com.google.common.collect.Lists
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger
import okhttp3.HttpUrl

import com.baulsupp.oksocial.output.util.FutureUtil.join
import java.util.stream.Collectors.toList

class GoogleDiscoveryCompleter(private val discoveryRegistry: DiscoveryRegistry,
                               private val discoveryDocPaths: List<String>) : ApiCompleter {
    private val mappings = CompletionMappings()

    init {

        initMappings()
    }

    private fun initMappings() {
        mappings.withVariable("userId", Lists.newArrayList("me"))
    }

    @Throws(IOException::class)
    override fun prefixUrls(): CompletableFuture<UrlList> {
        // not supported for partial urls
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
        val futures = discoveryDocPaths.stream().map<CompletableFuture<List<String>>>(Function<String, CompletableFuture<List<String>>> { this.singleFuture(it) }).collect<List<CompletableFuture<List<String>>>, Any>(toList())

        return join(futures).thenApply<UrlList>(Function<List<List<String>>, UrlList> { this.flattenList(it) }).thenCompose(Function<UrlList, CompletionStage<UrlList>> { mappings.replaceVariables(it) })
    }

    private fun flattenList(l: List<List<String>>): UrlList {
        return UrlList(UrlList.Match.SITE, l.stream().flatMap<String>(Function<List<String>, Stream<out String>> { it.stream() }).collect<List<String>, Any>(toList()))
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
