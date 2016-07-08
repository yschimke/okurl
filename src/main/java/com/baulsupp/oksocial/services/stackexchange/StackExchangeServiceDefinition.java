package com.baulsupp.oksocial.services.stackexchange;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class StackExchangeServiceDefinition implements ServiceDefinition<StackExchangeToken> {
  @Override public String apiHost() {
    return "api.stackexchange.com";
  }

  @Override public String serviceName() {
    return "StackExchange API";
  }

  @Override public String shortName() {
    return "stackexchange";
  }

  public StackExchangeToken parseCredentialsString(String s) {
    String[] parts = s.split(":", 2);
    return new StackExchangeToken(parts[0], parts[1]);
  }

  public String formatCredentialsString(StackExchangeToken credentials) {
    return credentials.accessToken + ":" + credentials.key;
  }
}
