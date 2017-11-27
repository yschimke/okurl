package com.baulsupp.oksocial.authenticator

import com.baulsupp.oksocial.output.util.JsonUtil
import com.baulsupp.oksocial.util.ClientException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object AuthUtil {
  suspend fun makeSimpleRequest(client: OkHttpClient, request: Request): String {
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

  suspend fun makeSimpleGetRequest(client: OkHttpClient, uri: String): String =
          makeSimpleRequest(client, uriGetRequest(uri))

  fun uriGetRequest(uri: String): Request = Request.Builder().url(uri).build()

  suspend fun makeJsonMapRequest(client: OkHttpClient, request: Request): Map<String, Any> =
          JsonUtil.map(makeSimpleRequest(client, request))

}
