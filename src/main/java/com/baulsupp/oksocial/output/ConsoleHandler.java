package com.baulsupp.oksocial.output;

import com.baulsupp.oksocial.util.UsageException;
import com.baulsupp.oksocial.util.Util;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
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
import static com.baulsupp.oksocial.output.OutputUtil.isJson;
import static com.baulsupp.oksocial.output.OutputUtil.isMedia;
import static com.baulsupp.oksocial.output.OutputUtil.isMediaType;
import static com.baulsupp.oksocial.output.OutputUtil.systemOut;
import static com.baulsupp.oksocial.util.CommandUtil.isInstalled;
import static com.baulsupp.oksocial.util.CommandUtil.isTerminal;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class ConsoleHandler implements OutputHandler {
  private final boolean showHeaders;

  public ConsoleHandler(boolean showHeaders) {
    this.showHeaders = showHeaders;
  }

  @Override public void showError(Throwable e) {
    if (e instanceof UsageException) {
      System.err.println(e.getMessage());
    } else {
      e.printStackTrace();
    }
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

    if (contentType != null) {
      if (isMedia(contentType)) {
        openPreview(response);
        return;
      } else if (isInstalled("jq") && (isJson(contentType) || isKnownJsonApi(response))) {
        prettyPrintJson(response);
        return;
      }
    }

    // TODO support a nice hex mode for binary files
    writeToSink(response.body().source(), systemOut());
    System.out.println("");
  }

  private boolean isKnownJsonApi(Response response) {
    // TODO move to authenticators
    return response.request().url().host().equals("graph.facebook.com") && isMediaType(
        response.body().contentType(), "text/javascript");
  }

  private void prettyPrintJson(Response response) throws IOException {
    List<String> command = isTerminal() ? asList("jq", "-C", ".") : asList("jq", ".");
    streamToCommand(Optional.of(response.body().source()), command);
  }

  public static void openPreview(Response response) throws IOException {
    if (Util.isOSX()) {
      streamToCommand(Optional.of(response.body().source()),
          asList("open", "-f", "-a", "/Applications/Preview.app"));
    } else if (Desktop.isDesktopSupported()) {
      File tempFile = writeToFile(response);

      Desktop.getDesktop().open(tempFile);
    } else if (isInstalled("img2txt")) {
      // TODO mplayer -really-quiet -vo caca ~/Movies/*
      File tempFile = writeToFile(response);

      String columns = System.getenv("COLUMNS");
      if (columns == null) {
        columns = "80";
      }

      streamToCommand(Optional.empty(),
          asList("img2txt", "-W", columns, "-f", "utf8", tempFile.getPath()));
    } else {
      System.err.println("Falling back to console output, use -r to avoid warning");

      writeToSink(response.body().source(), systemOut());
      System.out.println("");
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

  public static void streamToCommand(Optional<BufferedSource> source, List<String> command)
      throws IOException {
    try {
      ProcessExecutor pe = new ProcessExecutor().command(command)
          .redirectOutput(System.out)
          .timeout(5, TimeUnit.SECONDS)
          .redirectError(Slf4jStream.ofCaller().asInfo());

      if (source.isPresent()) {
        pe.redirectInput(source.get().inputStream());
      }

      ProcessResult pr = pe.execute();

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
