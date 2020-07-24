package com.baulsupp.okurl.services.google.indexer

import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.SimpleResponseExtractor
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.location.Location
import com.baulsupp.okurl.moshi.Rfc3339InstantJsonAdapter
import com.baulsupp.okurl.services.google.model.DiscoveryApis
import com.baulsupp.okurl.services.google.model.DiscoveryDoc
import com.baulsupp.okurl.services.google.model.DiscoveryIndexMap
import com.baulsupp.okurl.services.mapbox.model.MapboxLatLongAdapter
import com.baulsupp.okurl.util.ClientException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import java.io.File
import java.time.Instant
import java.util.Date

object UpdateIndexes : CommandLineClient() {
  override fun name() = "UpdateIndexed"

  @JvmStatic
  fun main(args: Array<String>) {
    this.initialise()

    val discoveryJsonUrl = "https://content.googleapis.com/discovery/v1/apis"

    val simpleOutput = ConsoleHandler(SimpleResponseExtractor)
    Main.moshi = Moshi.Builder()
      .add(Location::class.java, MapboxLatLongAdapter().nullSafe())
      .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
      .add(Instant::class.java, Rfc3339InstantJsonAdapter.nullSafe())
      .build()!!

    runBlocking {
      withContext(Dispatchers.IO) {
        try {
          val discoveryApis = client.query<DiscoveryApis>(request(discoveryJsonUrl))

          val items = discoveryApis.items.orEmpty()

          val jobs = items.map { item ->
            async {
              try {
                val discoveryDoc = client.query<DiscoveryDoc>(request(item.discoveryRestUrl))
                Pair(discoveryDoc.baseUrl, item.discoveryRestUrl)
              } catch (e: ClientException) {
                simpleOutput.showError("Failed to get discovery doc: ${item.discoveryRestUrl}", e)
                null
              }
            }
          }
            .mapNotNull { it.await() }

          val result = DiscoveryIndexMap(jobs.groupBy({ (rootUrl, _) ->
            rootUrl
          }, { (_, restUrl) ->
            restUrl
          }))

          val discoveryIndexSink =
            File("src/main/resources/com/baulsupp/okurl/services/google/index.json").sink()
          val buffer = discoveryIndexSink.buffer()
          Main.moshi.adapter(DiscoveryIndexMap::class.java)
            .indent("  ")
            .toJson(buffer, result)
          buffer.flush()
        } finally {
          close()
        }
      }
    }
  }
}
