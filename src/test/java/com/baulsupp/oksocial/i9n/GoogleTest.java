package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.services.github.GithubAuthInterceptor;
import com.google.common.collect.Lists;
import org.junit.Test;

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
    main.urlCompletion = "https://people.googleapis.com/";

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://people.googleapis.com/"));
  }

  @Test public void completePeopleEndpointPath() throws Throwable {
    main.urlCompletion = "https://people.googleapis.com/v1/people:batch";

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://people.googleapis.com/v1/people:batchGet"));
  }
}
