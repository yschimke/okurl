package com.baulsupp.oksocial.authenticator.oauth2;

import com.baulsupp.oksocial.AbstractServiceDefinition;

public class Oauth2ServiceDefinition extends AbstractServiceDefinition<Oauth2Token> {
  public Oauth2ServiceDefinition(String apiHost, String serviceName, String shortName,
      String apiDocs) {
    super(apiHost, serviceName, shortName, apiDocs);
  }

  public Oauth2Token parseCredentialsString(String s) {
    String[] parts = s.split(":");

    if (parts.length < 4) {
      return new Oauth2Token(parts[0]);
    } else {
      return new Oauth2Token(parts[0], parts[1], parts[2], parts[3]);
    }
  }

  public String formatCredentialsString(Oauth2Token credentials) {
    if (credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent()) {
      return credentials.accessToken
          + ":"
          + credentials.refreshToken.get()
          + ":"
          + credentials.clientId.get()
          + ":"
          + credentials.clientSecret.get();
    } else {
      return credentials.accessToken;
    }
  }
}
