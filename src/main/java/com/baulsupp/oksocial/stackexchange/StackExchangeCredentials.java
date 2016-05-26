package com.baulsupp.oksocial.stackexchange;

public class StackExchangeCredentials {
  public String serverToken;

  public StackExchangeCredentials() {
  }

  public StackExchangeCredentials(String serverToken) {
    this.serverToken = serverToken;
  }

  @Override
  public String toString() {
    return "StackExchangeCredentials{"
        + "serverToken='"
        + serverToken
        + '\''
        + '}';
  }
}
