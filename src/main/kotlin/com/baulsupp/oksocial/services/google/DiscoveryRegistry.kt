package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.credentials.Token
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.queryMap
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.services.google.model.DiscoveryDoc
import com.baulsupp.oksocial.util.FileUtil
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class DiscoveryRegistry(private val client: OkHttpClient) {
  val newClient by lazy {
    client.newBuilder().cache(Cache(File(FileUtil.oksocialSettingsDir, "google-cache"), MEBIBYTES.toBytes(20))).build()
  }

  suspend fun load(discoveryDocPath: String, tokenSet: Token): DiscoveryDocument {
    val map = newClient.query<DiscoveryDoc>(request(discoveryDocPath, tokenSet) {
      cacheControl(CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build())
    })

    return DiscoveryDocument(map)
  }
}
