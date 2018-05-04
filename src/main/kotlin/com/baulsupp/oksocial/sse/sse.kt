package com.baulsupp.oksocial.sse

import com.baulsupp.oksocial.kotlin.moshi
import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.TweakedEventSource
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

inline fun <reified T> messageHandler(crossinline handler: (T) -> Unit): EventHandler {
  return object : SseEventHandler() {
    override fun onMessage(event: String, messageEvent: MessageEvent) {
      val m = moshi.adapter(T::class.java).fromJson(messageEvent.data) as T
      handler.invoke(m)
    }
  }
}

fun OkHttpClient.newSse(handler: EventHandler, uri: HttpUrl): EventSource {
  val builder = EventSource.Builder(handler, uri.uri()).client(this).readTimeoutMs(60000)
  val source = TweakedEventSource(builder)
  source.start()

  return source
}
