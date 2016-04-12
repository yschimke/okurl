package com.baulsupp.oksocial.credentials;

public interface ServiceCredentials<T> {
  String apiHost();

  String serviceName();

  T parseCredentialsString(String s);

  String formatCredentialsString(T credentials);
}
