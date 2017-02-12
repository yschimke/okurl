package com.baulsupp.oksocial.commands;

import com.baulsupp.oksocial.completion.ArgumentCompleter;
import com.baulsupp.oksocial.completion.UrlCompleter;
import java.util.List;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.Optional.empty;

public interface ShellCommand {
  String name();

  List<Request> buildRequests(OkHttpClient client, Request.Builder requestBuilder,
      List<String> arguments) throws Exception;

  default Optional<String> authenticator() {
    return empty();
  }

  default boolean handlesRequests() {
    return false;
  }

  default Optional<ArgumentCompleter> completer() {
    return empty();
  }
}
