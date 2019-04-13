package com.baulsupp.okurl.sse

import com.baulsupp.okurl.kotlin.moshi
import com.baulsupp.okurl.kotlin.request
import com.baulsupp.okurl.kotlin.statusMessage
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

inline fun <reified T> messageHandler(crossinline handler: (T) -> Unit): EventSourceListener {
  return object : EventSourceListener() {
    override fun onEvent(
      eventSource: EventSource,
      id: String?,
      type: String?,
      data: String
    ) {
      val m = moshi.adapter(T::class.java).fromJson(data) as T
      handler.invoke(m)
    }

    override fun onOpen(eventSource: EventSource, response: Response) {
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
      when {
        t != null -> println(t)
        response != null -> println(response.statusMessage())
        else -> println("unknown failure")
      }
    }

    override fun onClosed(eventSource: EventSource) {
      println("closed")
    }
  }
}

fun OkHttpClient.newSse(handler: EventSourceListener, url: HttpUrl): EventSource {
  val req = request {
    url(url)
  }
  return EventSources.createFactory(this).newEventSource(req, handler)
}

fun Response.handleSseResponse(handler: EventSourceListener) {
  EventSources.processResponse(this, handler)
}
