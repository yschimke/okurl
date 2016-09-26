package com.baulsupp.oksocial.authenticator.basic;

import com.baulsupp.oksocial.AbstractServiceDefinition;
import com.baulsupp.oksocial.authenticator.BasicCredentials;

public class BasicAuthServiceDefinition extends AbstractServiceDefinition<BasicCredentials> {
  public BasicAuthServiceDefinition(String apiHost, String serviceName, String shortName) {
    super(apiHost, serviceName, shortName);
  }

  public BasicCredentials parseCredentialsString(String s) {
    String[] parts = s.split(":", 2);
    return new BasicCredentials(parts[0], parts[1]);
  }

  public String formatCredentialsString(BasicCredentials credentials) {
    return credentials.user + ":" + credentials.password;
  }
}
