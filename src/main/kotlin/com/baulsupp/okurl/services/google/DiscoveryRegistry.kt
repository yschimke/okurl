package com.baulsupp.okurl.services.google

import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.services.google.model.DiscoveryDoc
import com.baulsupp.okurl.util.FileUtil
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class DiscoveryRegistry(private val client: OkHttpClient) {
  val newClient: OkHttpClient by lazy {
    client.newBuilder().cache(Cache(File(FileUtil.okurlSettingsDir, "google-cache"), MEBIBYTES.toBytes(20))).build()
  }

  suspend fun load(discoveryDocPath: String, tokenSet: Token): DiscoveryDocument {
    val map = newClient.query<DiscoveryDoc>(request(discoveryDocPath, tokenSet) {
      cacheControl(CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build())
    })

    return DiscoveryDocument(map)
  }
}
