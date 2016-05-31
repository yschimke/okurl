package com.baulsupp.oksocial.output;

import com.baulsupp.oksocial.util.Util;
import java.awt.Desktop;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
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
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import static com.baulsupp.oksocial.output.DownloadHandler.writeToSink;
import static com.baulsupp.oksocial.output.OutputUtil.systemOut;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static okio.Okio.buffer;
import static okio.Okio.source;

public class ConsoleHandler implements OutputHandler {
  private final boolean showHeaders;

  public ConsoleHandler(boolean showHeaders) {
    this.showHeaders = showHeaders;
  }

  @Override
  public void showOutput(Response response) throws IOException {
    if (showHeaders) {
      System.out.println(StatusLine.get(response));
      Headers headers = response.headers();
      for (int i = 0, size = headers.size(); i < size; i++) {
        System.out.println(headers.name(i) + ": " + headers.value(i));
      }
      System.out.println();
    }

    MediaType contentType = response.body().contentType();

    if (contentType != null && isMediaType(contentType)) {
      openPreview(response);
    } else {
      // TODO support a nice hex mode for binary files
      writeToSink(response.body().source(), systemOut());
      System.out.println("");
    }
  }

  private boolean isMediaType(MediaType contentType) {
    return "image".equals(contentType.type()) || "pdf".equals(contentType.subtype());
  }

  public static void openPreview(Response response) throws IOException {
    if (Util.isOSX()) {
      streamToCommand(response.body().source(),
          asList("open", "-f", "-a", "/Applications/Preview.app"));
    } else if (Desktop.isDesktopSupported()) {
      File tempFile = writeToFile(response);

      Desktop.getDesktop().open(tempFile);
    } else {
      // TODO mplayer -really-quiet -vo caca ~/Movies/*
      File tempFile = writeToFile(response);

      try (BufferedSource fileSource = buffer(source(tempFile))) {
        streamToCommand(fileSource, asList("img2txt", "-f", "utf8", tempFile.getPath()));
      }
    }
  }

  private static File writeToFile(Response response) throws IOException {
    File tempFile =
        File.createTempFile("oksocial", OutputUtil.getExtension(response.body().contentType()));

    try (Sink out = Okio.sink(tempFile)) {
      writeToSink(response.body().source(), out);
    }
    return tempFile;
  }

  public static void streamToCommand(BufferedSource source, List<String> command)
      throws IOException {
    try {
      ProcessResult pr = new ProcessExecutor().command(command)
          .redirectOutput(System.out)
          .timeout(5, TimeUnit.SECONDS)
          .redirectInput(source.inputStream())
          .redirectError(Slf4jStream.ofCaller().asInfo())
          .execute();

      if (pr.getExitValue() != 0) {
        throw new IOException(
            "return code " + pr.getExitValue() + " from " + command.stream().collect(joining(" ")));
      }
    } catch (InterruptedException | TimeoutException e) {
      throw new IOException(e);
    }
  }

  public static void openLink(String url) throws IOException {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(URI.create(url));
    } else {
      System.err.println(url);
    }
  }
}
