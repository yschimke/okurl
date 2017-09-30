package com.baulsupp.oksocial.completion

import io.github.vjames19.futures.jdk8.ImmediateFuture
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class CompletionMappings {
    private val mappings = mutableListOf<(UrlList) -> CompletableFuture<UrlList>>()

    fun withVariable(name: String, values: List<String>) {
        withVariable(name, { completedFuture(values) })
    }

    fun withVariable(name: String, values: () -> CompletableFuture<List<String>>) {
        val element = { ul: UrlList -> values().map { l -> ul.replace(name, l, true) } }
        mappings.add(element)
    }

    fun replaceVariables(urlList: UrlList): CompletableFuture<UrlList> {
        var future = ImmediateFuture { urlList }

        for (s in mappings) {
            future = future.flatMap { s(it) }
        }

        return future
    }
}
