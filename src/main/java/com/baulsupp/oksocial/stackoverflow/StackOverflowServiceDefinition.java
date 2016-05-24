package com.baulsupp.oksocial.stackoverflow;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class StackOverflowServiceDefinition implements ServiceDefinition<StackOverflowCredentials> {
  @Override
  public String apiHost() {
    return "api.stackexchange.com";
  }

  @Override
  public String serviceName() {
    return "StackExchange API";
  }

  public StackOverflowCredentials parseCredentialsString(String s) {
    return new StackOverflowCredentials(s);
  }

  public String formatCredentialsString(StackOverflowCredentials credentials) {
    return credentials.serverToken;
  }
}
