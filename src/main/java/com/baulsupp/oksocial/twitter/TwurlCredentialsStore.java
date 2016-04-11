package com.baulsupp.oksocial.twitter;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;

public class TwurlCredentialsStore implements CredentialsStore<TwitterCredentials> {
  private final File file;

  private ServiceCredentials<TwitterCredentials> serviceCredentials = new TwitterOSXCredentials();

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

  @Override public String credentialsString(TwitterCredentials credentials) {
    return serviceCredentials.formatCredentialsString(credentials);
  }

  @Override public String apiHost() {
    return serviceCredentials.apiHost();
  }

  @Override public String serviceName() {
    return serviceCredentials.serviceName();
  }

  public TwitterCredentials readDefaultCredentials() {
    TwurlRc twurlRc = readTwurlRc();

    if (twurlRc != null) {
      String username = twurlRc.defaultProfile().get(0);
      String consumerKey = twurlRc.defaultProfile().get(1);

      return twurlRc.readCredentials(username, consumerKey);
    } else {
      return null;
    }
  }

  public TwitterCredentials readCredentials(String username, String consumerKey)
      throws IOException {
    TwurlRc twurlRc = readTwurlRc();

    if (twurlRc != null) {
      return twurlRc.readCredentials(username, consumerKey);
    } else {
      return null;
    }
  }

  @Override public void storeCredentials(TwitterCredentials credentials) {
    throw new UnsupportedOperationException();
  }
}
