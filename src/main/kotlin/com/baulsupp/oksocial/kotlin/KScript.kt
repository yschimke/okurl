package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.services.mapbox.model.MapboxLatLongAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val okshell: OkShell by lazy { OkShell.instance ?: OkShell.create() }

val client: OkHttpClient by lazy { okshell.commandLine.client!! }

val moshi = Moshi.Builder()
        .add(MapboxLatLongAdapter())
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()!!

fun warmup(vararg urls: String) {
  okshell.warmup(*urls)
}

fun location(): Location? = okshell.location()

fun show(url: String) {
  okshell.show(url)
}

fun newWebSocket(url: String, listener: WebSocketListener): WebSocket = client.newWebSocket(Request.Builder().url(url).build(), listener)

var dateOnlyformat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

fun epochSecondsToDate(seconds: Long) = dateOnlyformat.format(Date(seconds * 1000))!!

val terminalWidth: Int by lazy { (okshell.commandLine.outputHandler as ConsoleHandler).terminalWidth() }

fun jsonPostRequest(url: String, body: String): Request =
        Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json"), body)).build()

var arguments: List<String> = listOf()
