package com.baulsupp.oksocial.stackoverflow;

public class StackOverflowCredentials {
  public String serverToken;

  public StackOverflowCredentials() {
  }

  public StackOverflowCredentials(String serverToken) {
    this.serverToken = serverToken;
  }

  @Override
  public String toString() {
    return "StackOverflowCredentials{"
        + "serverToken='"
        + serverToken
        + '\''
        + '}';
  }
}
