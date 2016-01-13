package com.baulsupp.oksocial.credentials;

import java.io.IOException;

public interface CredentialsStore<T> {
  T readDefaultCredentials() throws IOException;

  void storeCredentials(T credentials) throws IOException;
}
