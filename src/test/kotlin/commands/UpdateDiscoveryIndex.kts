#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.client
import com.baulsupp.okurl.kotlin.execute
import com.baulsupp.okurl.kotlin.okshell
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.oksocial.output.writeToSink
import kotlinx.coroutines.runBlocking
import okio.sink
import java.io.File

val discoveryJsonUrl = "https://content.googleapis.com/discovery/v1/apis"

runBlocking {
  val response = client.execute(request(discoveryJsonUrl))

  val discoveryJsonSink = File("src/main/resources/com/baulsupp/okurl/services/google/discovery.json").sink()
  response.body()!!.source().writeToSink(discoveryJsonSink)
}

okshell.commandLine.close()
