package com.baulsupp.oksocial.services.uber;

import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class UberServiceDefinition implements ServiceDefinition<Oauth2Token> {
  @Override
  public String apiHost() {
    return "api.uber.com";
  }

  @Override
  public String serviceName() {
    return "Uber API";
  }

  public Oauth2Token parseCredentialsString(String s) {
    return new Oauth2Token(s);
  }

  public String formatCredentialsString(Oauth2Token credentials) {
    return credentials.accessToken;
  }
}
