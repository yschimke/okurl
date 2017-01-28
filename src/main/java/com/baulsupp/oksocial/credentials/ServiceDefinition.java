package com.baulsupp.oksocial.credentials;

import java.util.Optional;

import static java.util.Optional.empty;

public interface ServiceDefinition<T> {
  String apiHost();

  String serviceName();

  T parseCredentialsString(String s);

  String formatCredentialsString(T credentials);

  String shortName();

  default Optional<String> apiDocs() {
    return empty();
  }

  default Optional<String> accountsLink() {
    return empty();
  }
}
