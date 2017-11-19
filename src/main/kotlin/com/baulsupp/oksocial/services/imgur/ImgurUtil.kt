package com.baulsupp.oksocial.services.imgur

import okhttp3.Request

object ImgurUtil {

  val API_HOSTS = setOf((
      "api.imgur.com")
  )

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.imgur.com" + s).build()
  }
}
