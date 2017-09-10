package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture
import com.baulsupp.oksocial.output.util.JsonUtil
import com.baulsupp.oksocial.util.ClientException
import io.github.vjames19.futures.jdk8.ImmediateFuture
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.toCompletableFuture
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture

class JsonCredentialsValidator(
        private var request: Request,
        private var extractor: (Map<String, Any>) -> String,
        private var appRequest: Request? = null,
        private var appExtractor: ((Map<String, Any>) -> String)? = null) {
    init {
        if (appRequest == null) {
            appExtractor!!
        }
    }

    @Throws(IOException::class)
    fun validate(client: OkHttpClient): CompletableFuture<ValidatedCredentials> {
        val nameCallback = enqueue(client, request).flatMap { extractString(it, extractor) }

        val appCallback = appRequest?.let { r -> enqueue(client, r).flatMap { response -> extractString(response, appExtractor!!) } } ?: ImmediateFuture { null }

        return nameCallback.flatMap { name -> appCallback.map { app -> ValidatedCredentials(name, app) } }
    }

    private fun enqueue(client: OkHttpClient, r: Request?): CompletableFuture<Response> {
        val callback = OkHttpResponseFuture()
        client.newCall(r!!).enqueue(callback)
        return callback.future
    }

    private fun extractString(response: Response,
                              responseExtractor: (Map<String, Any>) -> String): CompletableFuture<String?> {
        try {
            val map = JsonUtil.map(response.body()!!.string())

            if (response.code() != 200) {
                var error = "verify failed"
                if (map.containsKey("error")) {
                    error += ": " + map["error"]
                }
                return ClientException(error, response.code()).toCompletableFuture()
            }

            return ImmediateFuture { responseExtractor(map) }
        } catch (e: IOException) {
            return e.toCompletableFuture()
        } finally {
            response.close()
        }
    }

    companion object {
        fun fieldExtractor(name: String) = { map: Map<String, Any> -> map[name] }
    }
}
