package com.baulsupp.oksocial.output;

import java.io.IOException;
import okhttp3.Response;

import static java.util.Arrays.asList;
import static java.util.Optional.of;

public class OsxOutputHandler extends ConsoleHandler {
  public void openPreview(Response response) throws IOException {
    streamToCommand(of(response.body().source()),
        asList("open", "-f", "-a", "/Applications/Preview.app"), of(30));
  }
}
