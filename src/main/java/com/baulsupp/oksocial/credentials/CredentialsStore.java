package com.baulsupp.oksocial.credentials;

import java.util.Optional;

public interface CredentialsStore {
  <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition);

  <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition);
}
