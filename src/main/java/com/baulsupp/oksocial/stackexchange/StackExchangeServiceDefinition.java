package com.baulsupp.oksocial.stackexchange;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class StackExchangeServiceDefinition implements ServiceDefinition<StackExchangeCredentials> {
  @Override
  public String apiHost() {
    return "api.stackexchange.com";
  }

  @Override
  public String serviceName() {
    return "StackExchange API";
  }

  public StackExchangeCredentials parseCredentialsString(String s) {
    return new StackExchangeCredentials(s);
  }

  public String formatCredentialsString(StackExchangeCredentials credentials) {
    return credentials.serverToken;
  }
}
