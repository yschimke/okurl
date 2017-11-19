package com.baulsupp.oksocial.services.foursquare

import okhttp3.Request

object FourSquareUtil {

  val API_HOSTS = setOf((
      "api.foursquare.com")
  )

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://api.foursquare.com" + s).build()
  }
}
