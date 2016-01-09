package com.baulsupp.oksocial;

import java.io.IOException;
import java.io.OutputStream;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

public class ConsoleHandler implements OutputHandler {
  private final boolean showHeaders;
  private final boolean openMedia;

  public ConsoleHandler(boolean showHeaders, boolean openMedia) {
    this.showHeaders = showHeaders;
    this.openMedia = openMedia;
  }

  @Override public void showOutput(Response response) throws IOException {
    try {
      if (showHeaders) {
        System.out.println(StatusLine.get(response));
        Headers headers = response.headers();
        for (int i = 0, size = headers.size(); i < size; i++) {
          System.out.println(headers.name(i) + ": " + headers.value(i));
        }
        System.out.println();
      }

      BufferedSource source = response.body().source();

      MediaType contentType = response.body().contentType();

      // TODO OSX only
      if (openMedia && Util.isOSX() && contentType.type().equals("image")) {
        openPreview(source);
      } else {
        writeToConsole(source);
      }
    } finally {
      response.body().close();
    }
  }

  private void openPreview(BufferedSource source) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("open", "-f", "-a", "/Applications/Preview.app");

    Process process = pb.start();

    try {
      OutputStream processStdin = process.getOutputStream();

      Sink out = Okio.sink(processStdin);

      writeToSink(source, out);

      processStdin.close();

      try {
        int result = process.waitFor();

        if (result != 0) {
          System.err.println("preview returned " + result);
        }
      } catch (InterruptedException e) {
        throw new IOException(e);
      }
    } finally {
      if (process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }

  /**
   * Stream the response to the System.out as it is returned from the server.
   */
  private void writeToConsole(BufferedSource source) throws IOException {
    // TODO support a nice hex mode for binary files

    Sink out = Okio.sink(System.out);
    writeToSink(source, out);

    // TODO is this always needed?
    System.out.println();
  }

  private void writeToSink(BufferedSource source, Sink out) throws IOException {
    while (!source.exhausted()) {
      out.write(source.buffer(), source.buffer().size());
      out.flush();
    }
  }
}
