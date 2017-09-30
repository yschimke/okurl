package com.baulsupp.oksocial.completion

import io.github.vjames19.futures.jdk8.ImmediateFuture
import io.github.vjames19.futures.jdk8.onSuccess
import java.util.concurrent.CompletableFuture

interface CompletionVariableCache {

    operator fun get(service: String, key: String): List<String>?

    fun store(service: String, key: String, values: List<String>)

    fun compute(service: String, key: String,
                s: () -> CompletableFuture<List<String>>): CompletableFuture<List<String>> {
        val values = get(service, key)

        return if (values != null) {
            ImmediateFuture { values!! }
        } else {
            s().onSuccess { store(service, key, it) }
        }
    }

    companion object {
        val NONE: CompletionVariableCache = object : CompletionVariableCache {
            override fun get(service: String, key: String): List<String>? {
                return null
            }

            override fun store(service: String, key: String, values: List<String>) {}
        }
    }
}
