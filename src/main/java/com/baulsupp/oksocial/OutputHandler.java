package com.baulsupp.oksocial;

import java.io.IOException;
import okhttp3.Response;

public interface OutputHandler {
  void showOutput(Response response) throws IOException;
}
