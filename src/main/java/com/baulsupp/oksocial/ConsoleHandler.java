package com.baulsupp.oksocial;

import java.io.IOException;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

public class ConsoleHandler implements OutputHandler {
  private final boolean showHeaders;

  public ConsoleHandler(boolean showHeaders) {
    this.showHeaders = showHeaders;
  }

  @Override public void showOutput(Response response) throws IOException {
    if (showHeaders) {
      System.out.println(StatusLine.get(response));
      Headers headers = response.headers();
      for (int i = 0, size = headers.size(); i < size; i++) {
        System.out.println(headers.name(i) + ": " + headers.value(i));
      }
      System.out.println();
    }

    // Stream the response to the System.out as it is returned from the server.
    Sink out = Okio.sink(System.out);
    BufferedSource source = response.body().source();
    while (!source.exhausted()) {
      out.write(source.buffer(), source.buffer().size());
      out.flush();
    }

    response.body().close();
  }
}
