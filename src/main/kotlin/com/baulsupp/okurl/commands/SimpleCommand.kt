package com.baulsupp.okurl.commands

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.kotlin.request
import okhttp3.OkHttpClient
import okhttp3.Request

open class SimpleCommand(val name: String, private val prefix: String, val authenticator: String?) : ShellCommand {

  override fun name(): String {
    return name
  }

  override fun buildRequests(
    client: OkHttpClient,
    arguments: List<String>
  ): List<Request> {
    return try {
      arguments.map { u -> request(prefix + u) }
    } catch (iae: IllegalArgumentException) {
      throw UsageException(iae.message.orEmpty())
    }
  }
}
