package com.baulsupp.oksocial.services.transferwise;

import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;

public class TransferwiseTestAuthInterceptor extends TransferwiseAuthInterceptor {
  @Override protected String host() {
    return "test-restgw.transferwise.com";
  }

  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition(host(), "Transferwise Test API", "transferwise-test");
  }


}
