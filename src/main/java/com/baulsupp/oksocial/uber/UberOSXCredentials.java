package com.baulsupp.oksocial.uber;

import com.baulsupp.oksocial.credentials.ServiceCredentials;

public class UberOSXCredentials implements ServiceCredentials<UberServerCredentials> {
  @Override public String apiHost() {
    return "api.uber.com";
  }

  @Override public String serviceName() {
    return "Uber API";
  }

  public UberServerCredentials parseCredentialsString(String s) {
    return new UberServerCredentials(s);
  }

  public String formatCredentialsString(UberServerCredentials credentials) {
    return credentials.serverToken;
  }
}
