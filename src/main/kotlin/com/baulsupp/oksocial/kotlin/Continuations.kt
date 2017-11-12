package com.baulsupp.oksocial.kotlin

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

suspend fun Call.await(): Response {
  return kotlinx.coroutines.experimental.suspendCancellableCoroutine { c ->
    enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        c.resumeWithException(e)
      }

      override fun onResponse(call: Call, response: Response) {
        c.resume(response)
      }
    })
  }
}