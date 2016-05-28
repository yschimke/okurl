package com.baulsupp.oksocial.authenticator.oauth2;

public class Oauth2Token {
  public String accessToken;

  public Oauth2Token() {
  }

  public Oauth2Token(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    return "Oauth2Token{"
        + "accessToken='"
        + accessToken
        + '\''
        + '}';
  }
}
