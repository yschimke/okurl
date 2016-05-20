package com.baulsupp.oksocial.commands;

import java.util.List;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public interface ShellCommand {
  String name();

  List<Request> buildRequests(OkHttpClient.Builder clientBuilder,
      Request.Builder requestBuilder, List<String> arguments) throws Exception;

  default Optional<String> authenticator() {
    return Optional.empty();
  }
}
