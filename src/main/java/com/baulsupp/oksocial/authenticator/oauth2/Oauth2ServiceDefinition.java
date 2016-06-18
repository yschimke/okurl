package com.baulsupp.oksocial.authenticator.oauth2;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class Oauth2ServiceDefinition implements ServiceDefinition<Oauth2Token> {
  private String apiHost;
  private String serviceName;
  private final String shortName;

  public Oauth2ServiceDefinition(String apiHost, String serviceName, String shortName) {
    this.apiHost = apiHost;
    this.serviceName = serviceName;
    this.shortName = shortName;
  }

  @Override public String shortName() {
    return shortName;
  }

  @Override
  public String apiHost() {
    return apiHost;
  }

  @Override
  public String serviceName() {
    return serviceName;
  }

  public Oauth2Token parseCredentialsString(String s) {
    String[] parts = s.split(":");

    if (parts.length == 1) {
      return new Oauth2Token(s);
    } else {
      return new Oauth2Token(parts[0], parts[1]);
    }
  }

  public String formatCredentialsString(Oauth2Token credentials) {
    return credentials.accessToken + (credentials.refreshToken.map(s -> ":" + s).orElse(""));
  }
}
