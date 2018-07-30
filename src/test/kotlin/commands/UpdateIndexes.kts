#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.moshi
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.kotlin.simpleOutput
import com.baulsupp.okurl.services.google.model.DiscoveryApis
import com.baulsupp.okurl.services.google.model.DiscoveryDoc
import com.baulsupp.okurl.services.google.model.DiscoveryIndexMap
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import okio.buffer
import okio.sink
import java.io.File

val discoveryJsonUrl = "https://content.googleapis.com/discovery/v1/apis"

runBlocking {
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
  }.mapNotNull { it.await() }

  val result = DiscoveryIndexMap(jobs.groupBy({ (rootUrl, _) ->
    rootUrl
  }, { (_, restUrl) ->
    restUrl
  }))

  val discoveryIndexSink = File("src/main/resources/com/baulsupp/okurl/services/google/index.json").sink()
  val buffer = discoveryIndexSink.buffer()
  moshi.adapter(DiscoveryIndexMap::class.java).indent("  ").toJson(buffer, result)
  buffer.flush()
}
