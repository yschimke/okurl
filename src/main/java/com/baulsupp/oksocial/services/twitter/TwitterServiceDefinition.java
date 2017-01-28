package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.Optional.of;

public class TwitterServiceDefinition implements ServiceDefinition<TwitterCredentials> {
  @Override public String apiHost() {
    return "api.twitter.com";
  }

  @Override public String serviceName() {
    return "Twitter API";
  }

  @Override public String shortName() {
    return "twitter";
  }

  @Override public Optional<String> apiDocs() {
    return of("https://apps.twitter.com/");
  }

  @Override public Optional<String> accountsLink() {
    return of("");
  }

  public TwitterCredentials parseCredentialsString(String s) {
    List<String> list = Splitter.on(",").splitToList(s);

    if (list.size() != 5) {
      throw new IllegalStateException("can't split '" + s + "'");
    }

    return new TwitterCredentials(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
  }

  public String formatCredentialsString(TwitterCredentials credentials) {
    return new StringJoiner(",")
        .add(credentials.username)
        .add(credentials.consumerKey)
        .add(credentials.consumerSecret)
        .add(credentials.token)
        .add(credentials.secret)
        .toString();
  }
}
