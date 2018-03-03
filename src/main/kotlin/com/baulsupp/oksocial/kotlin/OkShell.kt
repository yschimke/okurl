package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.commands.CommandLineClient
import com.baulsupp.oksocial.location.Location
import okhttp3.Request

class OkShell(val commandLine: CommandLineClient) {
  fun warmup(vararg urls: String) {
    client.warmup(*urls)
  }

  fun show(url: String) {
    val request = request(url)

    val call = client.newCall(request)

    val response = call.execute()

    commandLine.outputHandler.showOutput(response)
  }

  fun credentials(name: String): Any? {
    val interceptor = commandLine.authenticatingInterceptor.getByName(name)

    if (interceptor != null) {
      return commandLine.credentialsStore.get(interceptor.serviceDefinition(), commandLine.tokenSet)
    }

    return null
  }

  suspend fun location(): Location? = commandLine.locationSource.read()

  companion object {
    var instance: OkShell? = null
    fun create(): OkShell {
      val main = Main()
      main.initialise()
      return OkShell(commandLine = main)
    }
  }
}
