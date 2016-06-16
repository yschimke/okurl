package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.output.OutputHandler;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import okhttp3.Response;

public class TestOutputHandler implements OutputHandler {
  public final List<Response> responses = Lists.newArrayList();
  public final List<Throwable> failures = Lists.newArrayList();

  @Override public void showOutput(Response response) throws IOException {
    responses.add(response);
  }

  @Override public void showError(Throwable e) {
    failures.add(e);
  }
}
