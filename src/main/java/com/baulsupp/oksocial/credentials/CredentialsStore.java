package com.baulsupp.oksocial.credentials;

public interface CredentialsStore<T> {
  T readDefaultCredentials();

  void storeCredentials(T credentials);

  static <R> CredentialsStore<R> create(ServiceCredentials<R> serviceCredentials) {
    // TODO platform support
    return new OSXCredentialsStore<R>(serviceCredentials);
  }
}
