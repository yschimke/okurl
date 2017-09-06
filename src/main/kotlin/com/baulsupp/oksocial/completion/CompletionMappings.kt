package com.baulsupp.oksocial.completion

import com.google.common.collect.Lists
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Supplier

import java.util.concurrent.CompletableFuture.completedFuture

class CompletionMappings {
    private val mappings = Lists.newArrayList<Function<UrlList, CompletableFuture<UrlList>>>()

    fun withVariable(name: String, values: List<String>) {
        withVariable(name, { completedFuture(values) })
    }

    fun withVariable(name: String, values: Supplier<CompletableFuture<List<String>>>) {
        mappings.add({ ul -> values.get().thenApply<UrlList> { l -> ul.replace(name, l, true) } })
    }

    fun replaceVariables(urlList: UrlList): CompletableFuture<UrlList> {
        var future = CompletableFuture.completedFuture(urlList)

        for (s in mappings) {
            future = future.thenCompose(s)
        }

        return future
    }
}
