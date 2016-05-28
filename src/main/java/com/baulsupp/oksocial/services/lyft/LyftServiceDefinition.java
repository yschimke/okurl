package com.baulsupp.oksocial.services.lyft;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public class LyftServiceDefinition implements ServiceDefinition<LyftServerCredentials> {
  @Override
  public String apiHost() {
    return "api.lyft.com";
  }

  @Override
  public String serviceName() {
    return "Lyft API";
  }

  public LyftServerCredentials parseCredentialsString(String s) {
    return new LyftServerCredentials(s);
  }

  public String formatCredentialsString(LyftServerCredentials credentials) {
    return credentials.serverToken;
  }
}
