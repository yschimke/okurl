package com.baulsupp.okurl.ws

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.Main
import com.baulsupp.okurl.commands.MainAware
import com.baulsupp.okurl.commands.ShellCommand
import com.baulsupp.okurl.kotlin.request
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Scanner

class OkWsCommand : ShellCommand, MainAware {
  init {
    println("OkWsCommand")
    Exception().printStackTrace()
  }

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

    val request = request(arguments[0])

    val printer = WebSocketPrinter(main!!.outputHandler)
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
