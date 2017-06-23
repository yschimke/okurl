package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.i9n.TestCredentialsStore;
import com.google.common.collect.Lists;
import com.baulsupp.oksocial.output.TestOutputHandler;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleCompletionTest {
  private Main main = new Main();
  private TestOutputHandler<Response> output = new TestOutputHandler<Response>();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completePeopleEndpointSite() throws Throwable {
    assumeHasNetwork();

    main.arguments = newArrayList("https://people.googleapis.com/");
    main.urlComplete = true;

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://people.googleapis.com/"));
  }

  @Test public void completePeopleEndpointPath() throws Throwable {
    assumeHasNetwork();

    main.arguments = newArrayList("https://people.googleapis.com/v1/people:batch");
    main.urlComplete = true;

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://people.googleapis.com/v1/people:batchGet"));
  }

  @Test public void completeGmailUserId() throws Throwable {
    assumeHasNetwork();

    main.arguments = newArrayList("https://www.googleapis.com/gmail/v1/");
    main.urlComplete = true;

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(
        output.stdout.get(0).contains("https://www.googleapis.com/gmail/v1/users/me/profile"));
  }

  // Nested example
  @Test public void completeGmailMessages() throws Throwable {
    assumeHasNetwork();

    main.arguments = newArrayList("https://www.googleapis.com/gmail/v1/");
    main.urlComplete = true;

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(
        output.stdout.get(0).contains("https://www.googleapis.com/gmail/v1/users/me/messages"));
  }
}
