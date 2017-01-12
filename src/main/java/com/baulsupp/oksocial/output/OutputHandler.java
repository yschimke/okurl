package com.baulsupp.oksocial.output;

import java.io.IOException;
import okhttp3.Response;

public interface OutputHandler {
  void showOutput(Response response, boolean showHeaders) throws IOException;

  default void showError(String message, Throwable e) {
    System.err.println(message);
    e.printStackTrace();
  }

  default void openLink(String url) throws IOException {
    System.err.println(url);
  }

  default void info(String message) {
    System.out.println(message);
  }
}
