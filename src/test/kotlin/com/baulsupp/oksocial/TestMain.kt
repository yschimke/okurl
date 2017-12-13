package com.baulsupp.oksocial

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object TestMain {
  @Throws(Exception::class)
  @JvmStatic
  fun main(args: Array<String>) {
//    Main.main("https://graph.facebook.com/me")

    val client = OkHttpClient()
    val body = RequestBody.create(MediaType.parse("application/json"), "{}".toByteArray(Charsets.US_ASCII))
    val request = Request.Builder().url("https://httpbin.org/post").post(body).build()
    var response = client.newCall(request).execute()

    println(response.body()!!.string())
  }
}
