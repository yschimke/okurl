package com.baulsupp.oksocial.credentials;

import com.baulsupp.oksocial.util.Util;
import java.util.Optional;

public interface CredentialsStore<T> {
  Optional<T> readDefaultCredentials();

  void storeCredentials(T credentials);

  static <R> CredentialsStore<R> create(ServiceDefinition<R> serviceDefinition) {
    if (Util.isOSX()) {
      return new OSXCredentialsStore<R>(serviceDefinition);
    } else {
      return new PreferencesCredentialsStore<R>(serviceDefinition);
    }
  }

  ServiceDefinition<T> getServiceDefinition();
}
