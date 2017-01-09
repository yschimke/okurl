package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.basic.BasicAuthServiceDefinition;
import com.baulsupp.oksocial.services.twilio.TwilioAuthInterceptor;
import com.google.common.collect.Lists;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TwilioTest {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();
  private BasicAuthServiceDefinition service = new TwilioAuthInterceptor().serviceDefinition();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEndpointShortCommand1() throws Throwable {
    credentialsStore.storeCredentials(new BasicCredentials("ABC", "PW"), service);

    main.commandName = "okapi";
    main.arguments = newArrayList("commands/twilioapi", "/");
    main.urlComplete = true;

    main.run();

    assertEquals(newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("/Calls.json"));
  }

  @Test public void completeEndpointWithReplacements() throws Throwable {
    credentialsStore.storeCredentials(new BasicCredentials("ABC", "PW"), service);

    main.arguments = newArrayList("https://api.twilio.com/");
    main.urlComplete = true;

    main.run();

    assertEquals(newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("/Accounts/ABC/Calls.json"));
  }
}
