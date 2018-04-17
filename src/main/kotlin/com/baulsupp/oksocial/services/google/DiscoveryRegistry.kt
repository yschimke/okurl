package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.util.FileUtil
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

private val cacheControl = CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build()

private val cache = Cache(File(FileUtil.oksocialSettingsDir, "google-cache"), MEBIBYTES.toBytes(20))

class DiscoveryRegistry(private val client: OkHttpClient) {
  val newClient by lazy { client.newBuilder().cache(cache).build() }

  suspend fun load(discoveryDocPath: String, tokenSet: Token): DiscoveryDocument {
    val map = newClient.queryMap<Any>(request(discoveryDocPath, tokenSet) {
      cacheControl(cacheControl)
    })

    return DiscoveryDocument(map)
  }
}
