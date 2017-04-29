package com.baulsupp.oksocial.commands;

import com.baulsupp.oksocial.output.util.UsageException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SimpleCommand implements ShellCommand {
  private String name;
  private String prefix;
  private String authName;

  public SimpleCommand(String name, String prefix, String authName) {
    this.name = name;
    this.prefix = prefix;
    this.authName = authName;
  }

  @Override public String name() {
    return name;
  }

  public String mapUrl(String url) {
    return prefix + url;
  }

  @Override public List<Request> buildRequests(OkHttpClient clientBuilder,
      Request.Builder requestBuilder, List<String> urls) {
    try {
      return urls.stream().map(u -> requestBuilder.url(mapUrl(u)).build()).collect(
          Collectors.toList());
    } catch (IllegalArgumentException iae) {
      throw new UsageException(iae.getMessage());
    }
  }

  @Override public Optional<String> authenticator() {
    return Optional.ofNullable(authName);
  }
}
