package com.baulsupp.okurl.sse

import com.baulsupp.schoutput.handler.OutputHandler
import com.baulsupp.okurl.kotlin.isInteractive
import com.baulsupp.okurl.kotlin.success
import kotlinx.coroutines.runBlocking
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener

class SseOutput(val outputHandler: OutputHandler<*>) : EventSourceListener() {
  override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
    runBlocking {
      if (isInteractive()) {
        type?.let { outputHandler.showError(it.success()) }
      }
      // TODO mimetype formatting
      outputHandler.info(data)
    }
  }
}
