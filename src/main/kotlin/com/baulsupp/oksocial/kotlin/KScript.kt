package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.jjs.OkShell
import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.services.mapbox.MapboxLatLongAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import okhttp3.Request
import java.io.IOException
import java.util.Date

val okshell: OkShell = OkShell.instance()

val x = run {
  okshell.listenForMainExit()
}

val moshi = Moshi.Builder()
    .add(MapboxLatLongAdapter())
    .add(KotlinJsonAdapterFactory())
    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
    .build()

fun warmup(vararg urls: String) {
  urls.forEach { okshell.warmup(it) }
}

fun location(): Location? = okshell.location()

suspend fun show(url: String) {
  okshell.show(url)
}

@Throws(IOException::class)
inline suspend fun <reified T> query(url: String): T {
  val stringResult = okshell.execute(okshell.requestBuilder.url(url).build())

  return moshi.adapter(T::class.java).fromJson(stringResult)!!
}

@Throws(IOException::class)
suspend fun queryForString(url: String): String {
  return okshell.execute(okshell.requestBuilder.url(url).build())
}

@Throws(IOException::class)
suspend fun execute(request: Request): String {
  val call = okshell.client.newCall(request)

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
