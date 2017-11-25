package com.baulsupp.oksocial.kotlin

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

inline suspend fun <reified T> OkHttpClient.query(url: String): T {
  val stringResult = this.queryForString(okshell.requestBuilder.url(url).build())

  return moshi.adapter(T::class.java).fromJson(stringResult)!!
}

inline suspend fun <reified K, reified V> OkHttpClient.queryMap(request: Request): Map<K, V> {
  val stringResult = this.queryForString(request)

  val adapter = moshi.adapter<Any>(Types.newParameterizedType(Map::class.java, K::class.java, V::class.java)) as JsonAdapter<Map<K, V>>
  return adapter.fromJson(stringResult)!!
}

inline suspend fun <reified K, reified V> OkHttpClient.queryMap(url: String): Map<K, V> {
  return this.queryMap(okshell.requestBuilder.url(url).build())
}

inline suspend fun OkHttpClient.queryForString(request: Request): String {
  val response = this.execute(request)

  return response.body()!!.string()
}

inline suspend fun OkHttpClient.queryForString(url: String): String {
  return this.queryForString(okshell.requestBuilder.url(url).build())
}

suspend fun OkHttpClient.execute(request: Request): Response {
  val call = this.newCall(request)

  val response = call.await()

  if (!response.isSuccessful) {
    val responseString = response.body()!!.string()

    val msg: String = if (responseString.isNotEmpty()) {
      responseString
    } else {
      response.code().toString() + " " + response.message()
    }

    throw RuntimeException(msg)
  }

  return response
}