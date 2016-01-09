package com.baulsupp.oksocial.twitter;

import java.io.IOException;

public interface CredentialsStore {
  TwitterCredentials readDefaultCredentials() throws IOException;

  void storeCredentials(TwitterCredentials credentials) throws IOException;
}
