#!/usr/bin/env okscript

import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.okshell
import com.baulsupp.oksocial.kotlin.query
import com.baulsupp.oksocial.kotlin.newWebSocket
import com.baulsupp.oksocial.ws.WebSocketPrinter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.experimental.runBlocking
import com.baulsupp.oksocial.services.slack.model.RtmConnect

val wsClient = client.newBuilder().readTimeout(0, TimeUnit.MINUTES).build();

val start = runBlocking { wsClient.query<RtmConnect>("https://slack.com/api/rtm.connect") }

val printer = WebSocketPrinter(okshell.commandLine.outputHandler)
val ws = newWebSocket(start.url, printer)

printer.waitForExit()
