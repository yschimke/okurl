package com.baulsupp.oksocial.services.linkedin

import okhttp3.Request

object LinkedinUtil {

  val API_HOSTS = setOf((
      "api.linkedin.com"))

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.linkedin.com" + s).build()
  }
}
