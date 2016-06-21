package com.baulsupp.oksocial.authenticator.oauth2;

import java.util.Optional;

public class Oauth2Token {
  public String accessToken;
  public Optional<String> refreshToken;
  public Optional<String> clientId;
  public Optional<String> clientSecret;

  public Oauth2Token(String accessToken) {
    this.accessToken = accessToken;
    this.refreshToken = Optional.empty();
  }

  public Oauth2Token(String accessToken, String refreshToken, String clientId, String clientSecret) {
    this.accessToken = accessToken;
    this.refreshToken = Optional.ofNullable(refreshToken);
    this.clientId = Optional.ofNullable(clientId);
    this.clientSecret = Optional.ofNullable(clientSecret);
  }
}
