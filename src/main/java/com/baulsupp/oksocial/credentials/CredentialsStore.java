package com.baulsupp.oksocial.credentials;

public interface CredentialsStore<T> {
  T readDefaultCredentials();

  void storeCredentials(T credentials);

  static <R> CredentialsStore<R> create(ServiceDefinition<R> serviceDefinition) {
    // TODO platform support
    return new OSXCredentialsStore<R>(serviceDefinition);
  }

  ServiceDefinition<T> getServiceDefinition();
}
