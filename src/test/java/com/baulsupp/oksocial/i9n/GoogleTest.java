package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import org.junit.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GoogleTest {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
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
}
