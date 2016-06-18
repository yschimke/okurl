package com.baulsupp.oksocial.services.sheetsu;

import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class SheetsuServiceDefinition implements ServiceDefinition<BasicCredentials> {
  @Override
  public String apiHost() {
    return "sheetsu.com";
  }

  @Override
  public String serviceName() {
    return "Sheetsu API";
  }

  @Override public String shortName() {
    return "sheetsu";
  }

  public BasicCredentials parseCredentialsString(String s) {
    String[] parts = s.split(":", 2);
    return new BasicCredentials(parts[0], parts[1]);
  }

  public String formatCredentialsString(BasicCredentials credentials) {
    return credentials.user + ":" + credentials.password;
  }
}
