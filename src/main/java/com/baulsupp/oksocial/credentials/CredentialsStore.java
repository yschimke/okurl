package com.baulsupp.oksocial.credentials;

public interface CredentialsStore<T> {

  String apiHost();

  String serviceName();

  T readDefaultCredentials();

  void storeCredentials(T credentials);

  static <R> CredentialsStore<R> create(ServiceDefinition<R> serviceDefinition) {
    // TODO platform support
    return new OSXCredentialsStore<R>(serviceDefinition);
  }

  String credentialsString(T credentials);
}
