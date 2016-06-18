package com.baulsupp.oksocial.output;

import java.io.IOException;
import okhttp3.Response;

public interface OutputHandler {
  void showOutput(Response response) throws IOException;

  default void showError(Throwable e) {
    e.printStackTrace();
  }

  default void openLink(String url) throws IOException {
    System.err.println(url);
  }
}
