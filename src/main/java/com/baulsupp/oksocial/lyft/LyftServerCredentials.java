package com.baulsupp.oksocial.lyft;

public class LyftServerCredentials {
  public String serverToken;

  public LyftServerCredentials() {
  }

  public LyftServerCredentials(String serverToken) {
    this.serverToken = serverToken;
  }

  @Override
  public String toString() {
    return "LyftServerCredentials{"
        + "serverToken='"
        + serverToken
        + '\''
        + '}';
  }
}
