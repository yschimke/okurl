#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.execute
import com.baulsupp.oksocial.kotlin.okshell
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.output.writeToSink
import kotlinx.coroutines.experimental.runBlocking
import okio.Okio
import java.io.File

val discoveryJsonUrl = "https://content.googleapis.com/discovery/v1/apis"

runBlocking {
  val response = client.execute(request(discoveryJsonUrl))

  val discoveryJsonSink = Okio.sink(File("src/main/resources/com/baulsupp/oksocial/services/google/discovery.json"))
  response.body()!!.source().writeToSink(discoveryJsonSink)
}

okshell.commandLine.closeClients()
