package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.credentials.ServiceCredentials;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.List;

public class TwitterOSXCredentials implements ServiceCredentials<TwitterCredentials> {
  @Override public String apiHost() {
    return "api.twitter.com";
  }

  @Override public String serviceName() {
    return "Twitter API";
  }

  public TwitterCredentials parseCredentialsString(String s) {
    List<String> list = Splitter.on(",").splitToList(s);

    if (list.size() != 5) {
      throw new IllegalStateException("can't split '" + s + "'");
    }

    return new TwitterCredentials(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
  }

  public String formatCredentialsString(TwitterCredentials credentials) {
    return Joiner.on(",")
        .join(credentials.username, credentials.consumerKey, credentials.consumerSecret,
            credentials.token, credentials.secret);
  }
}
