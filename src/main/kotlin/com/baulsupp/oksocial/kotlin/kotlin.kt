package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.DefaultToken
import com.baulsupp.oksocial.Token
import com.baulsupp.oksocial.util.ClientException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

inline fun <reified V> Moshi.mapAdapter() =
  this.adapter<Any>(Types.newParameterizedType(Map::class.java, String::class.java, V::class.java)) as JsonAdapter<Map<String, V>>

inline fun <reified V> Moshi.listAdapter() =
  this.adapter<Any>(Types.newParameterizedType(List::class.java, V::class.java)) as JsonAdapter<List<V>>

suspend inline fun <reified T> OkHttpClient.query(url: String, tokenSet: Token = DefaultToken): T {
  return this.query(request(url, tokenSet))
}

suspend inline fun <reified T> OkHttpClient.query(request: Request): T {
  val stringResult = this.queryForString(request)

  return moshi.adapter(T::class.java).fromJson(stringResult)!!
}

suspend inline fun <reified T> OkHttpClient.queryPages(url: String, paginator: T.() -> Pagination, tokenSet: Token = DefaultToken): List<T> {
  var page = query<T>(url, tokenSet)
  val resultList = mutableListOf(page)

  var pages = paginator(page)

  while (pages !== End) {
    if (pages is Next) {
      page = query(pages.url, tokenSet)
      resultList.add(page)
      pages = paginator(page)
    } else if (pages is Rest) {
      val deferList = pages.urls.map {
        async(CommonPool) {
          query<T>(it, tokenSet)
        }
      }
      deferList.forEach { resultList.add(it.await()) }
      break
    }
  }

  return resultList
}

suspend inline fun <reified V> OkHttpClient.queryMap(request: Request): Map<String, V> {
  val stringResult = this.queryForString(request)

  return moshi.mapAdapter<V>().fromJson(stringResult)!!
}

suspend inline fun <reified V> OkHttpClient.queryMap(url: String, tokenSet: Token = DefaultToken): Map<String, V> =
  this.queryMap(request(url, tokenSet))

suspend inline fun <reified V> OkHttpClient.queryList(request: Request): List<V> {
  val stringResult = this.queryForString(request)

  return moshi.listAdapter<V>().fromJson(stringResult)!!
}

sealed class Pagination

object End : Pagination()

data class Next(val url: String) : Pagination()

data class Rest(val urls: List<String>) : Pagination()

suspend inline fun <reified V> OkHttpClient.queryList(url: String, tokenSet: Token = DefaultToken): List<V> =
  this.queryList(request(url, tokenSet))

suspend inline fun <reified V> OkHttpClient.queryOptionalMap(request: Request): Map<String, V>? {
  val stringResult = this.queryForString(request)

  return moshi.mapAdapter<V>().fromJson(stringResult)
}

suspend inline fun <reified V> OkHttpClient.queryOptionalMap(url: String, tokenSet: Token = DefaultToken): Map<String, V>? =
  this.queryOptionalMap(request(url, tokenSet))

suspend inline fun <reified T> OkHttpClient.queryMapValue(url: String, tokenSet: Token = DefaultToken, vararg keys: String): T? =
  this.queryMapValue<T>(request(url, tokenSet), *keys)

suspend inline fun <reified T> OkHttpClient.queryMapValue(request: Request, vararg keys: String): T? {
  val queryMap = this.queryMap<Any>(request)

  val result = keys.fold(queryMap as Any, { map, key -> (map as Map<String, Any>)[key]!! })

  return result as T
}

// TODO
fun HttpUrl.request(): Request = Request.Builder().url(this).build()

suspend fun OkHttpClient.queryForString(request: Request): String = execute(request).body()!!.string()

suspend fun OkHttpClient.queryForString(url: String, tokenSet: Token = DefaultToken): String =
  this.queryForString(request(url, tokenSet))

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
    val request = request(it, DefaultToken)
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

fun request(url: String, tokenSet: Token = DefaultToken) = requestBuilder(url, tokenSet).build()!!
fun request(url: HttpUrl, tokenSet: Token = DefaultToken) = requestBuilder(url, tokenSet).build()!!

fun requestBuilder(url: String, tokenSet: Token = DefaultToken) = Request.Builder().url(url).tag(tokenSet)
fun requestBuilder(url: HttpUrl, tokenSet: Token = DefaultToken) = Request.Builder().url(url).tag(tokenSet)
