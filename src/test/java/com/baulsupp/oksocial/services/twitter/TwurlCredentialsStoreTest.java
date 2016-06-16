package com.baulsupp.oksocial.services.twitter;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TwurlCredentialsStoreTest {
  @Test
  public void testReadDefaultCredentials() throws IOException {
    File file =
        new File(TwurlCredentialsStoreTest.class.getResource("/single_twurlrc.yaml").getFile());
    TwurlCredentialsStore store = new TwurlCredentialsStore(file);

    TwitterCredentials credentials = store.readCredentials().get();

    assertEquals("PROFILE", credentials.username);
    assertEquals("CONSUMER_KEY", credentials.consumerKey);
    assertEquals("CONSUMER_SECRET", credentials.consumerSecret);
    assertEquals("1234-TOKEN", credentials.token);
    assertEquals("SECRET", credentials.secret);
  }
}
