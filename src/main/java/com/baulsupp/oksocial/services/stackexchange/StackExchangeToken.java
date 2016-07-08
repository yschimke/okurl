package com.baulsupp.oksocial.services.stackexchange;

public class StackExchangeToken {
  public String accessToken;
  public String key;

  public StackExchangeToken() {
  }

  public StackExchangeToken(String accessToken, String key) {
    this.accessToken = accessToken;
    this.key = key;
  }
}
