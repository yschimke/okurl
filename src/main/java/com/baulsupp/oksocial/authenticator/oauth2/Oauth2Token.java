package com.baulsupp.oksocial.authenticator.oauth2;

import java.util.Optional;

public class Oauth2Token {
  public String accessToken;
  public Optional<String> refreshToken;

  public Oauth2Token(String accessToken) {
    this.accessToken = accessToken;
    this.refreshToken = Optional.empty();
  }

  public Oauth2Token(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = Optional.ofNullable(refreshToken);
  }

  @Override public String toString() {
    return "Oauth2Token{"
        + "accessToken='" + accessToken + '\''
        + refreshToken.map(s -> " refreshToken='" + s + '\'').orElse("")
        + '}';
  }
}
