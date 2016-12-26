package com.baulsupp.oksocial.output;

import com.baulsupp.oksocial.iterm.ItermOutputHandler;
import com.baulsupp.oksocial.util.UsageException;
import com.baulsupp.oksocial.util.Util;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Handshake;
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
  private static Logger logger = Logger.getLogger(ConsoleHandler.class.getName());

  public ConsoleHandler() {
  }

  @Override public void showError(String message, Throwable e) {
    if (logger.isLoggable(Level.FINE)) {
      logger.log(Level.WARNING, message, e);
    } else {
      if (e instanceof UsageException) {
        System.err.println(e.getMessage());
      } else if (e instanceof UnknownHostException && e.getCause() == null) {
        System.err.println(message + ": " + e.toString());
      } else {
        System.err.println(message);
        e.printStackTrace();
      }
    }
  }

  @Override public void showOutput(Response response, boolean showHeaders) throws IOException {
    if (showHeaders) {
      System.out.println(StatusLine.get(response));
      Headers headers = response.headers();
      for (int i = 0, size = headers.size(); i < size; i++) {
        System.out.println(headers.name(i) + ": " + headers.value(i));
      }
      System.out.println();
    }

    if (logger.isLoggable(Level.FINE)) {
      Handshake handshake = response.handshake();

      if (handshake != null) {
        logger.info("protocol: " + response.protocol());
        logger.info("tls: " + handshake.tlsVersion().toString());
        logger.info("cipher: " + handshake.cipherSuite().toString());
        logger.info("peer: " + handshake.peerPrincipal().toString());
      }
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
    streamToCommand(Optional.of(response.body().source()), command, Optional.empty());
  }

  public void streamToCommand(Optional<BufferedSource> source, List<String> command,
      Optional<Integer> timeout)
      throws IOException {
    try {
      ProcessExecutor pe = new ProcessExecutor().command(command)
          .redirectOutput(System.out)
          .redirectError(Slf4jStream.ofCaller().asInfo());

      timeout.ifPresent(integer -> pe.timeout(integer, TimeUnit.SECONDS));

      source.ifPresent(bufferedSource -> pe.redirectInput(bufferedSource.inputStream()));

      ProcessResult pr = pe.execute();

      if (pr.getExitValue() != 0) {
        throw new IOException(
            "return code " + pr.getExitValue() + " from " + command.stream().collect(joining(" ")));
      }
    } catch (InterruptedException | TimeoutException e) {
      throw new IOException(e);
    }
  }

  public void openPreview(Response response) throws IOException {
    if (Desktop.isDesktopSupported()) {
      File tempFile = writeToFile(response);

      Desktop.getDesktop().open(tempFile);
    } else {
      System.err.println("Falling back to console output, use -r to avoid warning");

      writeToSink(response.body().source(), systemOut());
      System.out.println("");
    }
  }

  public File writeToFile(Response response) throws IOException {
    File tempFile =
        File.createTempFile("oksocial", OutputUtil.getExtension(response.body().contentType()));

    try (Sink out = Okio.sink(tempFile)) {
      writeToSink(response.body().source(), out);
    }
    return tempFile;
  }

  @Override public void openLink(String url) throws IOException {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(URI.create(url));
    } else {
      System.err.println(url);
    }
  }

  public int terminalWidth() throws IOException {
    try {
      ProcessExecutor pe = new ProcessExecutor().command("/bin/stty", "-a", "-f", "/dev/tty")
          .timeout(5, TimeUnit.SECONDS)
          .redirectError(Slf4jStream.ofCaller().asInfo())
          .readOutput(true);

      String output = pe.execute().outputString();

      Pattern p = Pattern.compile("(\\d+) columns", Pattern.MULTILINE);

      Matcher m = p.matcher(output);
      if (m.find()) {
        return Integer.parseInt(m.group(1));
      } else {
        return 80;
      }
    } catch (InterruptedException ie) {
      throw new InterruptedIOException(ie.getMessage());
    } catch (TimeoutException e) {
      throw new IOException(e);
    }
  }

  public static ConsoleHandler instance() {
    if (ItermOutputHandler.isAvailable()) {
      return new ItermOutputHandler();
    } else if (Util.isOSX()) {
      return new OsxOutputHandler();
    } else {
      return new ConsoleHandler();
    }
  }
}
