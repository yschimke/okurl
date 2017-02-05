package com.baulsupp.oksocial.ws;

import com.baulsupp.oksocial.output.OutputHandler;
import java.util.concurrent.CountDownLatch;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketPrinter extends WebSocketListener {
  private CountDownLatch latch = new CountDownLatch(1);
  private OutputHandler outputHandler;

  public WebSocketPrinter(OutputHandler outputHandler) {
    this.outputHandler = outputHandler;
  }

  public void waitForExit() throws InterruptedException {
    latch.await();
  }

  @Override public void onMessage(WebSocket webSocket, String text) {
    outputHandler.info(text);
  }

  @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
    outputHandler.info(bytes.hex());
  }

  @Override public void onClosed(WebSocket webSocket, int code, String reason) {
    latch.countDown();
  }

  @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    outputHandler.showError(null, t);
    latch.countDown();
  }
}
