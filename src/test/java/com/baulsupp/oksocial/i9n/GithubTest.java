package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.services.github.GithubAuthInterceptor;
import com.baulsupp.oksocial.services.twilio.TwilioServiceDefinition;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    main.urlCompletion = "/";
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("commands/githubapi");

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("/user"));
  }

  @Test public void completeEndpoint() throws Throwable {
    credentialsStore.storeCredentials(new Oauth2Token("ABC"), service);

    main.urlCompletion = "https://api.github.com/";

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("https://api.github.com/user"));
  }
}
