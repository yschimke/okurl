package com.baulsupp.oksocial;

import okhttp3.Response;

import java.io.IOException;

public interface OutputHandler {
  void showOutput(Response response) throws IOException;
}
