package com.baulsupp.oksocial.completion

import java.io.IOException
import java.util.concurrent.CompletableFuture
import okhttp3.HttpUrl

interface ApiCompleter {
    @Throws(IOException::class)
    fun prefixUrls(): CompletableFuture<UrlList>

    @Throws(IOException::class)
    fun siteUrls(url: HttpUrl): CompletableFuture<UrlList>
}
