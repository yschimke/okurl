package com.baulsupp.oksocial.services.stackexchange;

import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class StackExchangeServiceDefinition implements ServiceDefinition<Oauth2Token> {
  @Override
  public String apiHost() {
    return "api.stackexchange.com";
  }

  @Override
  public String serviceName() {
    return "StackExchange API";
  }

  public Oauth2Token parseCredentialsString(String s) {
    return new Oauth2Token(s);
  }

  public String formatCredentialsString(Oauth2Token credentials) {
    return credentials.accessToken;
  }
}
