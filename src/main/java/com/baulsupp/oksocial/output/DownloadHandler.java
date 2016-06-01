package com.baulsupp.oksocial.output;

import java.io.File;
import java.io.IOException;
import java.util.List;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

import static com.baulsupp.oksocial.output.OutputUtil.systemOut;

public class DownloadHandler implements OutputHandler {
  private File outputFile;

  public DownloadHandler(File outputFile) {
    this.outputFile = outputFile;
  }

  @Override
  public void showOutput(Response response) throws IOException {
    BufferedSource source = response.body().source();

    Sink outputSink = getOutputSink(response);
    try {
      writeToSink(source, outputSink);
    } finally {
      if (!isStdout()) {
        outputSink.close();
      }
    }
  }

  public Sink getOutputSink(Response response) throws IOException {
    if (isStdout()) {
      return systemOut();
    } else if (outputFile.isDirectory()) {
      List<String> segments = response.request().url().pathSegments();
      String name = segments.get(segments.size() - 1);
      File responseOutputFile = new File(outputFile, name);
      System.err.println("Saving " + responseOutputFile);
      return Okio.sink(responseOutputFile);
    } else {
      if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
        if (!outputFile.getParentFile().mkdirs()) {
          throw new IOException("unable to create directory " + outputFile);
        }
      }
      return Okio.sink(outputFile);
    }
  }

  private boolean isStdout() {
    return outputFile.getPath().equals("-");
  }

  public static void writeToSink(BufferedSource source, Sink out) throws IOException {
    while (!source.exhausted()) {
      out.write(source.buffer(), source.buffer().size());
      out.flush();
    }
  }
}
