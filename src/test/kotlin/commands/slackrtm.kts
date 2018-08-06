#!/usr/bin/env okscript

import com.baulsupp.okurl.kotlin.*
import com.baulsupp.okurl.services.slack.model.RtmConnect
import com.baulsupp.okurl.ws.WebSocketPrinter
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

val wsClient = client.newBuilder().readTimeout(0, TimeUnit.MINUTES).build()

val start = runBlocking { wsClient.query<RtmConnect>("https://slack.com/api/rtm.connect") }

val printer = WebSocketPrinter(okshell.commandLine.outputHandler)
val ws = newWebSocket(start.url, printer)

printer.waitForExit()
