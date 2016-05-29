package com.baulsupp.oksocial;

import com.baulsupp.oksocial.util.Util;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

public class ConsoleHandler implements OutputHandler {
  private final boolean showHeaders;
  private final boolean openMedia;

  public ConsoleHandler(boolean showHeaders, boolean openMedia) {
    this.showHeaders = showHeaders;
    this.openMedia = openMedia;
  }

  @Override
  public void showOutput(Response response) throws IOException {
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
      if (openMedia && Util.isOSX() && contentType != null && isMediaType(contentType)) {
        openPreview(source);
      } else {
        writeToConsole(source);
      }
    } finally {
      response.body().close();
    }
  }

  private boolean isMediaType(MediaType contentType) {
    return "image".equals(contentType.type()) || "pdf".equals(contentType.subtype());
  }

  public static void openPreview(BufferedSource source) throws IOException {

    try {
      new ProcessExecutor().command("open", "-f", "-a", "/Applications/Preview.app")
          .timeout(5, TimeUnit.SECONDS)
          .redirectOutput(Slf4jStream.ofCaller().asInfo())
          .redirectInput(source.inputStream())
          .execute();
    } catch (InterruptedException | TimeoutException e) {
      throw new IOException(e);
    }
  }

  public static void openLink(String url) throws IOException {
    if (Util.isOSX()) {
      try {
        new ProcessExecutor().command("open", url)
            .timeout(5, TimeUnit.SECONDS)
            .redirectOutput(Slf4jStream.ofCaller().asInfo())
            .execute();
      } catch (InterruptedException | TimeoutException e) {
        throw new IOException(e);
      }
    } else {
      System.err.println(url);
    }
  }

  /**
   * Stream the response to the System.out as it is returned from the server.
   */
  private static void writeToConsole(BufferedSource source) throws IOException {
    // TODO support a nice hex mode for binary files

    Sink out = Okio.sink(System.out);
    writeToSink(source, out);

    // TODO is this always needed?
    System.out.println();
  }

  private static void writeToSink(BufferedSource source, Sink out) throws IOException {
    while (!source.exhausted()) {
      out.write(source.buffer(), source.buffer().size());
      out.flush();
    }
  }
}
