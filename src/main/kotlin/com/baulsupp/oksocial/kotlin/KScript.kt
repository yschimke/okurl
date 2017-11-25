package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.jjs.OkShell
import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.services.mapbox.MapboxLatLongAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import okhttp3.Request
import okhttp3.Response
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

inline suspend fun <reified T> query(url: String): T = okshell.client.query(url)

inline suspend fun <reified K, reified V> queryMap(url: String): Map<K, V> =
        okshell.client.queryMap<K, V>(url)

inline suspend fun queryForString(url: String): String = okshell.client.queryForString(url)

inline suspend fun execute(request: Request): Response = okshell.client.execute(request)
