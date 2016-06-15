package com.baulsupp.oksocial.credentials;

import com.baulsupp.oksocial.util.Util;
import java.util.Optional;

public interface CredentialsStore {
  <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition);

  <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition);

  static CredentialsStore create() {
    if (Util.isOSX()) {
      return new OSXCredentialsStore();
    } else {
      return new PreferencesCredentialsStore();
    }
  }
}
