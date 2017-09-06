package com.baulsupp.oksocial.completion

import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.stream.Stream
import okhttp3.HttpUrl

import java.util.concurrent.CompletableFuture.completedFuture
import java.util.stream.Collectors.toList

open class HostUrlCompleter(private val hosts: Collection<String>) : ApiCompleter {

    @Throws(IOException::class)
    override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
        return completedFuture(
                UrlList(UrlList.Match.SITE, urls(true)))
    }

    private fun urls(siteOnly: Boolean): List<String> {
        val f: Function<String, Stream<String>>
        if (siteOnly) {
            f = { h -> Stream.of("https://$h/") }
        } else {
            f = { h -> Stream.of("https://" + h, "https://$h/") }
        }

        return hosts.stream().flatMap(f).collect<List<String>, Any>(toList())
    }

    @Throws(IOException::class)
    override fun prefixUrls(): CompletableFuture<UrlList> {
        return completedFuture(
                UrlList(UrlList.Match.HOSTS, urls(false)))
    }
}
