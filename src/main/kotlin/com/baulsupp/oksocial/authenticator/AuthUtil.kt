package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.okhttp.OkHttpResponseFuture
import com.baulsupp.oksocial.output.util.JsonUtil
import com.baulsupp.oksocial.util.ClientException
import io.github.vjames19.futures.jdk8.map
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture

object AuthUtil {
  @Throws(IOException::class)
  fun makeSimpleRequest(client: OkHttpClient, request: Request): String {
    client.newCall(request).execute().use { response -> return responseToString(response) }
  }

  @Throws(IOException::class)
  fun responseToString(response: Response): String {
    if (!response.isSuccessful) {
      if (response.code() in 400..499) {
        throw ClientException(response.message(), response.code())
      }

      var message = response.body()!!.string()

      if (message.isEmpty()) {
        message = response.message()
      }

      throw IllegalStateException(
          "failed request " + response.code() + ": " + message)
    }

    return response.body()!!.string()
  }

  @Throws(IOException::class)
  fun makeSimpleGetRequest(client: OkHttpClient, uri: String): String {
    return makeSimpleRequest(client, uriGetRequest(uri))
  }

  fun uriGetRequest(uri: String): Request {
    return Request.Builder().url(uri).build()
  }

  @Throws(IOException::class)
  fun makeJsonMapRequest(client: OkHttpClient, request: Request): Map<String, Any> {
    return JsonUtil.map(makeSimpleRequest(client, request))
  }

  fun enqueueJsonMapRequest(client: OkHttpClient,
                            request: Request): CompletableFuture<Map<String, Any>> {
    val callback = OkHttpResponseFuture()
    client.newCall(request).enqueue(callback)

    return callback.future.map { JsonUtil.map(responseToString(it)) }
  }
}
