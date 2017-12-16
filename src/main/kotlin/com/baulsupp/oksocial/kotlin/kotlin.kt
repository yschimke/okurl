package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.util.ClientException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

inline suspend fun <reified T> OkHttpClient.query(url: String): T = this.query(Request.Builder().url(url).build())

inline suspend fun <reified T> OkHttpClient.query(request: Request): T {
  val stringResult = this.queryForString(request)

  return moshi.adapter(T::class.java).fromJson(stringResult)!!
}

inline suspend fun <reified V> OkHttpClient.queryMap(request: Request): Map<String, V> {
  val stringResult = this.queryForString(request)

  return moshi.mapAdapter<V>().fromJson(stringResult)!!
}

inline fun <reified V> Moshi.mapAdapter() =
        moshi.adapter<Any>(Types.newParameterizedType(Map::class.java, String::class.java, V::class.java)) as JsonAdapter<Map<String, V>>

inline suspend fun <reified V> OkHttpClient.queryMap(url: String): Map<String, V> =
        this.queryMap(Request.Builder().url(url).build())

inline suspend fun <reified T> OkHttpClient.queryMapValue(url: String, vararg keys: String): T? =
        this.queryMapValue<T>(Request.Builder().url(url).build(), *keys)

inline suspend fun <reified T> OkHttpClient.queryMapValue(request: Request, vararg keys: String): T? {
  val queryMap = this.queryMap<Any>(request)

  val result = keys.fold(queryMap as Any, { map, key -> (map as Map<String, Any>)[key]!! })

  return result as T
}

fun HttpUrl.request() = Request.Builder().url(this).build()

suspend fun OkHttpClient.queryForString(request: Request): String = execute(request).body()!!.string()

suspend fun OkHttpClient.queryForString(url: String): String =
        this.queryForString(Request.Builder().url(url).build())

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

    throw ClientException(msg, response.code())
  }

  return response
}

fun OkHttpClient.warmup(vararg urls: String) {
  urls.forEach {
    val request = Request.Builder().url(it).build()
    val call = this.newCall(request)
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        // ignore
      }

      override fun onResponse(call: Call, response: Response) {
        // ignore
        response.close()
      }
    })
  }
}
