package com.baulsupp.oksocial.services.uber

import okhttp3.Request

object UberUtil {

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.uber.com" + s).build()
  }
}
