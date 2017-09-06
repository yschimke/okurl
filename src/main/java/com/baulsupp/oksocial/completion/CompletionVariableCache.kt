package com.baulsupp.oksocial.completion

import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import java.util.concurrent.CompletableFuture.completedFuture

interface CompletionVariableCache {

    operator fun get(service: String, key: String): Optional<List<String>>

    fun store(service: String, key: String, values: List<String>)

    fun compute(service: String, key: String,
                s: Supplier<CompletableFuture<List<String>>>): CompletableFuture<List<String>> {
        val values = get(service, key)

        if (values.isPresent) {
            return completedFuture(values.get())
        } else {
            val x = s.get()
            x.thenAccept { l -> store(service, key, l) }
            return x
        }
    }

    companion object {
        val NONE: CompletionVariableCache = object : CompletionVariableCache {
            override fun get(service: String, key: String): Optional<List<String>> {
                return Optional.empty()
            }

            override fun store(service: String, key: String, values: List<String>) {}
        }
    }
}
