package com.baulsupp.oksocial.kotlin

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


@Throws(IOException::class)
inline suspend fun <reified T> OkHttpClient.query(url: String): T {
  val stringResult = this.execute(okshell.requestBuilder.url(url).build())

  return moshi.adapter(T::class.java).fromJson(stringResult)!!
}

@Throws(IOException::class)
inline suspend fun <reified K, reified V> OkHttpClient.queryMap(url: String): Map<K, V> {
  val stringResult = this.execute(okshell.requestBuilder.url(url).build())

  val adapter = moshi.adapter<Any>(Types.newParameterizedType(Map::class.java, K::class.java, V::class.java)) as JsonAdapter<Map<K, V>>
  return adapter.fromJson(stringResult)!!
}

@Throws(IOException::class)
suspend fun OkHttpClient.queryForString(url: String): String {
  return this.execute(okshell.requestBuilder.url(url).build())
}

@Throws(IOException::class)
suspend fun OkHttpClient.execute(request: Request): String {
  val call = this.newCall(request)

  val response = call.await()

  try {
    val responseString = response.body()!!.string()

    if (!response.isSuccessful) {
      val msg: String = if (responseString.isNotEmpty()) {
        responseString
      } else {
        response.code().toString() + " " + response.message()
      }

      throw RuntimeException(msg)
    }

    return responseString
  } finally {
    response.body()!!.close()
  }
}