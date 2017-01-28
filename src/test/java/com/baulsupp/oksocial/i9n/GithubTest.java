package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.services.github.GithubAuthInterceptor;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GithubTest {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();
  private Oauth2ServiceDefinition service = new GithubAuthInterceptor().serviceDefinition();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEndpointShortCommand1() throws Throwable {
    credentialsStore.storeCredentials(new Oauth2Token("ABC"), service);

    main.commandName = "okapi";
    main.arguments = newArrayList("commands/githubapi", "/");
    main.urlComplete = true;

    main.run();

    assertEquals(newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("/user"));
  }

  @Test public void completeEndpoint() throws Throwable {
    credentialsStore.storeCredentials(new Oauth2Token("ABC"), service);

    main.arguments = newArrayList("https://api.github.com/");
    main.urlComplete = true;

    main.run();

    assertEquals(newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://api.github.com/user"));
  }
}
