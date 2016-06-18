package com.baulsupp.oksocial.services.twitter.twurlrc;

import com.baulsupp.oksocial.services.twitter.TwitterCredentials;
import java.util.List;
import java.util.Map;

public class TwurlRc {
  public Map<String, Map<String, TwitterCredentials>> profiles;
  public Map<String, List<String>> configuration;

  public List<String> defaultProfile() {
    return configuration.get("default_profile");
  }

  public TwitterCredentials readCredentials(String username, String consumerKey) {
    return profiles.get(username).get(consumerKey);
  }
}
