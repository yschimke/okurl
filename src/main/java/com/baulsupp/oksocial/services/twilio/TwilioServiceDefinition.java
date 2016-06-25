package com.baulsupp.oksocial.services.twilio;

import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class TwilioServiceDefinition implements ServiceDefinition<BasicCredentials> {
  @Override public String apiHost() {
    return "api.twilio.com";
  }

  @Override public String serviceName() {
    return "Twilio API";
  }

  @Override public String shortName() {
    return "twilio";
  }

  public BasicCredentials parseCredentialsString(String s) {
    String[] parts = s.split(":", 2);
    return new BasicCredentials(parts[0], parts[1]);
  }

  public String formatCredentialsString(BasicCredentials credentials) {
    return credentials.user + ":" + credentials.password;
  }
}
