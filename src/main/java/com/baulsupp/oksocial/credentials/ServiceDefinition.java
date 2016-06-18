package com.baulsupp.oksocial.credentials;

public interface ServiceDefinition<T> {
  String apiHost();

  String serviceName();

  T parseCredentialsString(String s);

  String formatCredentialsString(T credentials);

  String shortName();
}
