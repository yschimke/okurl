package com.baulsupp.oksocial.services.twitter.twurlrc;

import com.baulsupp.oksocial.services.twitter.TwitterCredentials;
import com.baulsupp.oksocial.output.util.UsageException;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class TwurlrcImport {
  public static TwitterCredentials authorize(List<String> authArguments) {
    TwurlCredentialsStore twurlStore;

    if (authArguments.size() > 1) {
      twurlStore = new TwurlCredentialsStore(new File(authArguments.get(1)));
    } else {
      twurlStore = TwurlCredentialsStore.TWURL_STORE;
    }

    Optional<TwitterCredentials> credentials =
        twurlStore.readCredentials();

    if (!credentials.isPresent()) {
      throw new UsageException("No credentials found in " + twurlStore.getFile());
    }

    return credentials.get();
  }
}
