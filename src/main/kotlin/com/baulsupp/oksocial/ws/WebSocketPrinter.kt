package com.baulsupp.oksocial.ws

import com.baulsupp.oksocial.output.OutputHandler
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.CountDownLatch

class WebSocketPrinter(private val outputHandler: OutputHandler<Response>) : WebSocketListener() {
    private val latch = CountDownLatch(1)

    @Throws(InterruptedException::class)
    fun waitForExit() {
        latch.await()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        outputHandler.info(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        outputHandler.info(bytes.hex())
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        latch.countDown()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        outputHandler.showError(null, t)
        latch.countDown()
    }
}
