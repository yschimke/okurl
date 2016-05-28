package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class FacebookServiceDefinition implements ServiceDefinition<Oauth2Token> {
  @Override
  public String apiHost() {
    return "graph.facebook.com";
  }

  @Override
  public String serviceName() {
    return "Facebook API";
  }

  public Oauth2Token parseCredentialsString(String s) {
    return new Oauth2Token(s);
  }

  public String formatCredentialsString(Oauth2Token credentials) {
    return credentials.accessToken;
  }
}
