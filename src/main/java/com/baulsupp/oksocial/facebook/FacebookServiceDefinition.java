package com.baulsupp.oksocial.facebook;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class FacebookServiceDefinition implements ServiceDefinition<FacebookCredentials> {
  @Override
  public String apiHost() {
    return "graph.facebook.com";
  }

  @Override
  public String serviceName() {
    return "Facebook API";
  }

  public FacebookCredentials parseCredentialsString(String s) {
    return new FacebookCredentials(s);
  }

  public String formatCredentialsString(FacebookCredentials credentials) {
    return credentials.accessToken;
  }
}
