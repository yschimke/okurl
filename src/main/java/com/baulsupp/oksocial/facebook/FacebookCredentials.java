package com.baulsupp.oksocial.facebook;

public class FacebookCredentials {
  public String accessToken;

  public FacebookCredentials() {
  }

  public FacebookCredentials(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    return "FacebookCredentials{"
        + "accessToken='"
        + accessToken
        + '\''
        + '}';
  }
}
