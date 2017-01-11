package com.baulsupp.oksocial.credentials;

import java.util.Optional;

import static java.util.Optional.empty;

public interface CredentialsStore {
  CredentialsStore NONE = new CredentialsStore() {
    @Override
    public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
      return empty();
    }

    @Override
    public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    }
  };

  <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition);

  <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition);
}
