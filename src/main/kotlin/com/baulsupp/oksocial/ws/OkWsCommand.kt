package com.baulsupp.oksocial.ws

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.ShellCommand
import com.baulsupp.oksocial.output.util.UsageException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Scanner

class OkWsCommand : ShellCommand, MainAware {
  private var main: Main? = null

  override fun setMain(main: Main) {
    this.main = main
  }

  override fun name(): String {
    return "okws"
  }

  override fun handlesRequests(): Boolean {
    return true
  }

  override fun buildRequests(client: OkHttpClient, arguments: List<String>): List<Request> {
    if (arguments.size != 1) {
      throw UsageException("usage: okws wss://host")
    }

    val request = Request.Builder().url(arguments[0]).build()

    val printer = WebSocketPrinter(main!!.outputHandler!!)
    val websocket = client.newWebSocket(request, printer)

    val sc = Scanner(System.`in`)
    while (sc.hasNextLine()) {
      val line = sc.nextLine()
      websocket.send(line)
    }

    printer.waitForExit()

    return listOf()
  }
}
