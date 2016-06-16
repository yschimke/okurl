package com.baulsupp.oksocial.credentials;

import java.util.Optional;

public class FixedTokenCredentialsStore implements CredentialsStore {
  private String token;

  public FixedTokenCredentialsStore(String token) {
    this.token = token;
  }

  @Override public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
    return Optional.of(serviceDefinition.parseCredentialsString(token));
  }

  @Override
  public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    throw new UnsupportedOperationException();
  }
}
