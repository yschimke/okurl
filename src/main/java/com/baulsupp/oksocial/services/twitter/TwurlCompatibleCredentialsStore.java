package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import java.io.File;
import java.util.Optional;

public class TwurlCompatibleCredentialsStore implements CredentialsStore<TwitterCredentials> {
  private TwurlCredentialsStore twurlStore =
      new TwurlCredentialsStore(new File(System.getProperty("user.home"), ".twurlrc"));

  private CredentialsStore<TwitterCredentials> nativeStore =
      CredentialsStore.create(new TwitterServiceDefinition());

  private ServiceDefinition<TwitterCredentials> serviceDefinition = new TwitterServiceDefinition();

  @Override
  public Optional<TwitterCredentials> readDefaultCredentials() {
    Optional<TwitterCredentials> credentials = nativeStore.readDefaultCredentials();

    if (!credentials.isPresent()) {
      credentials = twurlStore.readDefaultCredentials();

      if (credentials.isPresent()) {
        nativeStore.storeCredentials(credentials.get());
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
