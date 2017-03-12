package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import ee.schimke.oksocial.output.TestOutputHandler;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpsCompletion {
  private Main main = new Main();
  private TestOutputHandler<Response> output = new TestOutputHandler<Response>();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completePeopleEndpointSite() throws Throwable {
    main.arguments = newArrayList("https://");
    main.urlComplete = true;

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://people.googleapis.com/"));
    assertTrue(output.stdout.get(0).contains("https://graph.facebook.com/"));
  }
}
