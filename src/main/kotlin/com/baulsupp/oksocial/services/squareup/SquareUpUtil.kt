package com.baulsupp.oksocial.services.squareup

import okhttp3.Request
import java.util.Arrays

object SquareUpUtil {

  val API_HOSTS = setOf((
      "connect.squareup.com")
  )

  var ALL_PERMISSIONS: Collection<String> = Arrays.asList(
      "MERCHANT_PROFILE_READ",
      "PAYMENTS_READ",
      "SETTLEMENTS_READ",
      "BANK_ACCOUNTS_READ"
  )

  fun apiRequest(s: String, requestBuilder: Request.Builder): Request {
    return requestBuilder.url("https://connect.squareup.com" + s).build()
  }
}
