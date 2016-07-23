package com.baulsupp.oksocial.output;

import java.io.IOException;
import java.util.Optional;
import okhttp3.Response;

import static java.util.Arrays.asList;

public class OsxOutputHandler extends ConsoleHandler {
  public void openPreview(Response response) throws IOException {
    streamToCommand(Optional.of(response.body().source()),
        asList("open", "-f", "-a", "/Applications/Preview.app"));
  }
}
