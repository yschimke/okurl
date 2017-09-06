package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture
import com.baulsupp.oksocial.util.ClientException
import com.baulsupp.oksocial.output.util.FutureUtil
import com.baulsupp.oksocial.output.util.JsonUtil
import java.io.IOException
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Future
import java.util.function.Function
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import java.util.Optional.empty
import java.util.Optional.of
import java.util.concurrent.CompletableFuture.completedFuture

class JsonCredentialsValidator {
    private var request: Request? = null
    private var extractor: Function<Map<String, Any>, String>? = null
    private var appRequest: Optional<Request>? = null
    private var appExtractor: Optional<Function<Map<String, Any>, String>>? = null

    constructor(request: Request,
                extractor: Function<Map<String, Any>, String>) {
        this.request = request
        this.extractor = extractor
        this.appRequest = empty()
        this.appExtractor = empty()
    }

    constructor(request: Request,
                extractor: Function<Map<String, Any>, String>, appRequest: Request,
                appExtractor: Function<Map<String, Any>, String>) {
        this.request = request
        this.extractor = extractor
        this.appRequest = Optional.of(appRequest)
        this.appExtractor = Optional.of(appExtractor)
    }

    @Throws(IOException::class)
    fun validate(client: OkHttpClient): Future<Optional<ValidatedCredentials>> {
        val nameCallback = enqueue(client, request).thenCompose { n -> extractString(n, extractor) }

        val appCallback = appRequest!!.map { r -> enqueue(client, r).thenCompose { response -> extractString(response, appExtractor!!.get()) } }
                .orElse(completedFuture(empty()))

        return nameCallback.thenCombine(appCallback
        ) { n, c -> n.map { a -> ValidatedCredentials(n, c) } }
    }

    private fun enqueue(client: OkHttpClient, r: Request?): CompletableFuture<Response> {
        val callback = OkHttpResponseFuture()
        client.newCall(r!!).enqueue(callback)
        return callback.future
    }

    private fun extractString(response: Response,
                              responseExtractor: Function<Map<String, Any>, String>?): CompletionStage<Optional<String>> {
        try {
            val map = JsonUtil.map(response.body()!!.string())

            if (response.code() != 200) {
                var error = "verify failed"
                if (map.containsKey("error")) {
                    error += ": " + map["error"]
                }
                return FutureUtil.failedFuture(ClientException(
                        error, response.code()))
            }

            val name = responseExtractor!!.apply(map)

            return completedFuture(of(name))
        } catch (e: IOException) {
            return FutureUtil.failedFuture(e)
        } finally {
            response.close()
        }
    }

    companion object {

        fun fieldExtractor(name: String): Function<Map<String, Any>, String> {
            return { map -> map.get(name) }
        }
    }
}
