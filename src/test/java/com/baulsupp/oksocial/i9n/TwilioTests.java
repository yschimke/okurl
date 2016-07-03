package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.services.twilio.TwilioServiceDefinition;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TwilioTests {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();
  private TwilioServiceDefinition service = new TwilioServiceDefinition();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEndpointShortCommand1() throws Throwable {
    credentialsStore.storeCredentials(new BasicCredentials("ABC", "PW"), service);

    main.urlCompletion = "/";
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("commands/twilioapi");

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertTrue(output.stdout.get(0).contains("/Calls.json"));
  }
}
