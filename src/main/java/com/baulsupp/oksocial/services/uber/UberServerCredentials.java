package com.baulsupp.oksocial.services.uber;

public class UberServerCredentials {
  public String serverToken;

  public UberServerCredentials() {
  }

  public UberServerCredentials(String serverToken) {
    this.serverToken = serverToken;
  }

  @Override
  public String toString() {
    return "UberServerCredentials{"
        + "serverToken='"
        + serverToken
        + '\''
        + '}';
  }
}
