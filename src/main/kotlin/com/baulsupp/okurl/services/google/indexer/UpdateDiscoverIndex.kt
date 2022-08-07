package com.baulsupp.okurl.services.google.indexer

import com.baulsupp.schoutput.writeToSink
import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.kotlin.execute
import com.baulsupp.okurl.kotlin.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.sink
import java.io.File

object UpdateDiscoverIndex: CommandLineClient() {
  override fun name() = "UpdateDiscoverIndex"

  @JvmStatic
  fun main(args: Array<String>) {
    this.initialise()

    val discoveryJsonUrl = "https://content.googleapis.com/discovery/v1/apis"

    runBlocking {
      val response = client.execute(request(discoveryJsonUrl))

      withContext(Dispatchers.IO) {
        val discoveryJsonSink =
          File("src/main/resources/com/baulsupp/okurl/services/google/discovery.json").sink()
        response.body.source().writeToSink(discoveryJsonSink)
      }
    }

    close()
  }
}
