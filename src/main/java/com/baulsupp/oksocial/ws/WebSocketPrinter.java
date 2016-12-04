package com.baulsupp.oksocial.ws;

import java.util.concurrent.CountDownLatch;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketPrinter extends WebSocketListener {
  private CountDownLatch latch = new CountDownLatch(1);

  public void waitForExit() throws InterruptedException {
    latch.await();
  }

  @Override public void onMessage(WebSocket webSocket, String text) {
    System.out.println(text);
  }

  @Override public void onClosed(WebSocket webSocket, int code, String reason) {
    latch.countDown();
  }

  @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    t.printStackTrace();
  }
}
