package com.baulsupp.oksocial.ws;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.Buffer;
import okio.ByteString;

public class WebSocketPrinter implements WebSocketListener {
  private CountDownLatch latch = new CountDownLatch(1);

  public void waitForExit() throws InterruptedException {
    latch.await();
  }

  @Override public void onOpen(WebSocket webSocket, Response response) {
  }

  @Override public void onFailure(Throwable e, Response response) {
    e.printStackTrace();
  }

  @Override public void onMessage(ResponseBody responseBody) throws IOException {
    System.out.println(responseBody.string());
  }

  @Override public void onPong(ByteString buffer) {
  }

  @Override public void onClose(int i, String s) {
    latch.countDown();
  }
}
