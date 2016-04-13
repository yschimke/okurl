package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.Util;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.OSXCredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;

import java.io.File;

public class TwurlCompatibleCredentialsStore implements CredentialsStore<TwitterCredentials> {
  private TwurlCredentialsStore twurlStore =
      new TwurlCredentialsStore(new File(System.getProperty("user.home"), ".twurlrc"));

  private OSXCredentialsStore<TwitterCredentials> nativeStore = null;

  private ServiceDefinition<TwitterCredentials> serviceDefinition = new TwitterServiceDefinition();

  public TwurlCompatibleCredentialsStore() {
    nativeStore = Util.isOSX() ? new OSXCredentialsStore<>(new TwitterServiceDefinition()) : null;
  }

  @Override
  public TwitterCredentials readDefaultCredentials() {
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

  @Override
  public ServiceDefinition<TwitterCredentials> getServiceDefinition() {
    return serviceDefinition;
  }

  @Override
  public void storeCredentials(TwitterCredentials credentials) {
    if (nativeStore != null) {
      nativeStore.storeCredentials(credentials);
    }
  }
}
