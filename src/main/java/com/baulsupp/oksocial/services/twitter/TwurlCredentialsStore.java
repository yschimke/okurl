package com.baulsupp.oksocial.services.twitter;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Throwables;
import java.io.File;
import java.util.Optional;

public class TwurlCredentialsStore implements CredentialsStore<TwitterCredentials> {
  private final File file;

  private ServiceDefinition<TwitterCredentials> serviceDefinition = new TwitterServiceDefinition();

  public TwurlCredentialsStore(File file) {
    this.file = file;
  }

  public TwurlRc readTwurlRc() {
    try {
      if (file.isFile()) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setPropertyNamingStrategy(
            PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        return objectMapper.readValue(file, TwurlRc.class);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public ServiceDefinition<TwitterCredentials> getServiceDefinition() {
    return serviceDefinition;
  }

  public Optional<TwitterCredentials> readDefaultCredentials() {
    TwurlRc twurlRc = readTwurlRc();

    if (twurlRc != null) {
      String username = twurlRc.defaultProfile().get(0);
      String consumerKey = twurlRc.defaultProfile().get(1);

      return Optional.ofNullable(twurlRc.readCredentials(username, consumerKey));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void storeCredentials(TwitterCredentials credentials) {
    throw new UnsupportedOperationException();
  }
}
