package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.Util;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import java.io.File;
import java.io.IOException;

public class TwurlCompatibleCredentialsStore implements CredentialsStore<TwitterCredentials> {
  private TwurlCredentialsStore twurlStore =
      new TwurlCredentialsStore(new File(System.getProperty("user.home"), ".twurlrc"));

  private OSXCredentialsStore<TwitterCredentials> nativeStore = null;

  public TwurlCompatibleCredentialsStore() {
    nativeStore = Util.isOSX() ? new OSXCredentialsStore<>(new TwitterOSXCredentials()) : null;
  }

  @Override public TwitterCredentials readDefaultCredentials() {
    TwitterCredentials credentials = null;

    if (nativeStore != null) {
      credentials = nativeStore.readDefaultCredentials();
    }

    if (credentials == null) {
      credentials = twurlStore.readDefaultCredentials();

      if (credentials != null && nativeStore != null) {
        nativeStore.storeCredentials(credentials);
      }
    }

    return credentials;
  }

  @Override public void storeCredentials(TwitterCredentials credentials) {
    if (nativeStore != null) {
      nativeStore.storeCredentials(credentials);
    }
  }
}
