package com.baulsupp.oksocial.services.giphy

import okhttp3.Request

object GiphyUtil {

  val API_HOSTS = setOf((
      "api.giphy.com")
  )

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.giphy.com" + s).build()
  }
}
