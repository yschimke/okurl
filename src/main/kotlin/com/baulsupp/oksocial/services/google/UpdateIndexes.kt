package com.baulsupp.oksocial.services.google

import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.moshi
import com.baulsupp.oksocial.kotlin.okshell
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.services.google.model.DiscoveryApis
import com.baulsupp.oksocial.services.google.model.DiscoveryDoc
import com.baulsupp.oksocial.services.google.model.DiscoveryIndexMap
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import okio.Okio
import java.io.File

fun main(args: Array<String>) {
  val discoveryJsonUrl = "https://content.googleapis.com/discovery/v1/apis"

  runBlocking {
    val discoveryApis = client.query<DiscoveryApis>(request(discoveryJsonUrl))

    val items = discoveryApis.items.orEmpty()

    val jobs = items.map { item ->
      async {
        try {
          val discoveryDoc = client.query<DiscoveryDoc>(request(item.discoveryRestUrl))
          Pair(discoveryDoc.rootUrl, item.discoveryRestUrl)
        } catch (e: Exception) {
          System.err.println(e.toString())
          null
        }
      }
    }.mapNotNull { it.await() }

    val result = DiscoveryIndexMap(jobs.groupBy({ (rootUrl, restUrl) ->
      rootUrl
    }, { (rootUrl, restUrl) ->
      restUrl
    }))

    println(result.index)

    val discoveryIndexSink = Okio.sink(File("src/main/resources/com/baulsupp/oksocial/services/google/index.json"))
    val buffer = Okio.buffer(discoveryIndexSink)
    moshi.adapter(DiscoveryIndexMap::class.java).indent("  ").toJson(buffer, result)
    buffer.flush()
  }

  okshell.commandLine.closeClients()
}
