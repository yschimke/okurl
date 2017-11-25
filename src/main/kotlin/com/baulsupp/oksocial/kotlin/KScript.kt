package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.jjs.OkShell
import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.services.mapbox.MapboxLatLongAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val okshell: OkShell by lazy { OkShell.instance }

val client: OkHttpClient by lazy { okshell.client }

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

suspend fun queryForString(url: String): String = okshell.client.queryForString(url)

fun newWebSocket(url: String, listener: WebSocketListener): WebSocket = okshell.client.newWebSocket(okshell.requestBuilder.url(url).build(), listener)

var dateOnlyformat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

fun epochSecondsToDate(seconds: Long) = dateOnlyformat.format(Date(seconds * 1000))

val terminalWidth: Int by lazy { (okshell.outputHandler as ConsoleHandler).terminalWidth() }


