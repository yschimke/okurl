package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.commands.ShellCommand;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PostTweet implements ShellCommand {
  @Override public String name() {
    return "tweet";
  }

  @Override public List<Request> buildRequests(OkHttpClient.Builder clientBuilder,
      Request.Builder requestBuilder, List<String> urls) {
    requestBuilder.url("https://api.twitter.com/1.1/statuses/update.json");

    String statusText = String.join(" ", urls);
    FormBody body = new FormBody.Builder().add("status", statusText).build();

    requestBuilder.post(body);

    Request request = requestBuilder.build();

    return Lists.newArrayList(request);
  }

  @Override public Optional<String> authenticator() {
    return Optional.of(TwitterAuthInterceptor.NAME);
  }
}
