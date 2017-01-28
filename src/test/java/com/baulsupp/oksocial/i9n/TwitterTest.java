package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TwitterTest {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
    main.resolve = Lists.newArrayList();
  }

  @Test public void setToken() throws Throwable {
    main.authorize = true;
    main.token = "PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET";
    main.arguments = Lists.newArrayList("twitter");

    main.run();

    if (!output.failures.isEmpty()) {
      throw output.failures.get(0);
    }

    assertEquals("PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET",
        credentialsStore.tokens.get("api.twitter.com"));
  }

  @Test public void importFromTwurl() throws Throwable {
    main.authorize = true;
    main.arguments =
        Lists.newArrayList("twitter", "--twurlrc", "src/test/resources/single_twurlrc.yaml");

    main.run();

    if (!output.failures.isEmpty()) {
      throw output.failures.get(0);
    }

    assertEquals("PROFILE,CONSUMER_KEY,CONSUMER_SECRET,1234-TOKEN,SECRET",
        credentialsStore.tokens.get("api.twitter.com"));
  }
}
