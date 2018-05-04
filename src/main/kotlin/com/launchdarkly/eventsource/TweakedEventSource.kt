package com.launchdarkly.eventsource

import com.baulsupp.oksocial.credentials.DefaultToken
import okhttp3.Request

class TweakedEventSource(builder: Builder): EventSource(builder) {
  override fun buildRequest(): Request {
    return super.buildRequest().newBuilder().tag(DefaultToken).build()
  }
}
