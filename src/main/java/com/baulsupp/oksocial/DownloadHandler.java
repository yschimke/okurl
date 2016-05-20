package com.baulsupp.oksocial;

import java.io.File;
import java.io.IOException;
import java.util.List;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

public class DownloadHandler implements OutputHandler {
  private File outputFile;

  public DownloadHandler(File outputFile) {
    this.outputFile = outputFile;
  }

  @Override
  public void showOutput(Response response) throws IOException {
    try {
      BufferedSource source = response.body().source();

      writeToSink(source, getOutputFile(response));
    } finally {
      response.body().close();
    }
  }

  public File getOutputFile(Response response) throws IOException {
    if (outputFile.isDirectory()) {
      List<String> segments = response.request().url().pathSegments();
      String name = segments.get(segments.size() - 1);
      return new File(outputFile, name);
    } else {
      if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
        if (!outputFile.getParentFile().mkdirs()) {
          throw new IOException("unable to create directory " + outputFile);
        }
      }
      return outputFile;
    }
  }

  private static void writeToSink(BufferedSource source, File thisOutputFile) throws IOException {
    System.err.println("Saving " + thisOutputFile);
    Sink out = Okio.sink(thisOutputFile);
    while (!source.exhausted()) {
      out.write(source.buffer(), source.buffer().size());
      out.flush();
    }
    out.close();
  }
}
