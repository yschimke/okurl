package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.baulsupp.oksocial.output.util.JsonUtil
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class DiscoveryRegistry(private val client: OkHttpClient, private val map: Map<String, Any>) {

    private val items: Map<String, Map<String, Any>>
        get() = map["items"] as Map<String, Map<String, Any>>

    fun load(discoveryDocPath: String): CompletableFuture<DiscoveryDocument> {
        val request = Request.Builder().url(discoveryDocPath).cacheControl(cacheControl).build()
        val mapFuture = AuthUtil.enqueueJsonMapRequest(client, request)

        return mapFuture.thenApply { s -> DiscoveryDocument(s) }
    }

    companion object {
        private val cache = Cache(File(System.getProperty("user.home"), ".oksocial/google-cache"),
                MEBIBYTES.toBytes(20))

        private val cacheControl = CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build()

        // TODO make non synchronous
        @Synchronized
        @Throws(IOException::class)
        fun instance(client: OkHttpClient): DiscoveryRegistry {
            var client = client
            client = client.newBuilder().cache(cache).build()

            val url = "https://www.googleapis.com/discovery/v1/apis"
            val request = Request.Builder().cacheControl(cacheControl).url(url).build()
            val response = client.newCall(request).execute()

            return response.use { response ->
                DiscoveryRegistry(client, JsonUtil.map(response!!.body()!!.string()))
            }
        }
    }
}
