package com.baulsupp.oksocial.completion

import okhttp3.HttpUrl
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.function.Supplier

class BaseUrlCompleter(private val urlList: UrlList, hosts: Collection<String>) : HostUrlCompleter(hosts) {
    private val mappings = CompletionMappings()

    @Throws(IOException::class)
    override fun siteUrls(url: HttpUrl): CompletableFuture<UrlList> {
        val future = completedFuture(urlList)

        return future.thenCompose { r -> mappings.replaceVariables(r) }
    }

    fun withVariable(name: String, values: List<String>) {
        mappings.withVariable(name, values)
    }

    fun withVariable(name: String, values: () -> CompletableFuture<List<String>>) {
        mappings.withVariable(name, values)
    }
}
