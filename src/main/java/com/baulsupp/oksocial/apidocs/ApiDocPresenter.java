package com.baulsupp.oksocial.apidocs;

import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import okhttp3.OkHttpClient;

public interface ApiDocPresenter {
  ApiDocPresenter NONE =
      (url, outputHandler, client) -> outputHandler.info("No documentation for: " + url);

  void explainApi(String url, OutputHandler outputHandler, OkHttpClient client) throws IOException;
}
