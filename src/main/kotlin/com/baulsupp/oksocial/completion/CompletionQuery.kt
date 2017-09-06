package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.authenticator.AuthUtil
import java.util.concurrent.CompletableFuture
import okhttp3.OkHttpClient
import okhttp3.Request

import java.util.stream.Collectors.toList

object CompletionQuery {
    fun getIds(client: OkHttpClient, urlString: String,
               path: String,
               key: String): CompletableFuture<List<String>> {
        val request = Request.Builder().url(urlString).build()

        return AuthUtil.enqueueJsonMapRequest(client, request)
                .thenApply { map ->
                    val surveys = map[path] as List<Map<String, Any>>
                    surveys.stream().map { m -> m[key] as String }.collect<List<String>, Any>(toList())
                }
    }
}
