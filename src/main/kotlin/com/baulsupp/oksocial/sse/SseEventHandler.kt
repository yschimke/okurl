package com.baulsupp.oksocial.sse

import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.MessageEvent

open class SseEventHandler : EventHandler {
  override fun onOpen() {
  }

  override fun onComment(comment: String) {
  }

  override fun onMessage(event: String, messageEvent: MessageEvent) {
  }

  override fun onClosed() {
  }

  override fun onError(t: Throwable) {
    t.printStackTrace()
  }
}
