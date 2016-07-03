package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompletionTests {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEmpty() throws Throwable {
    main.urlCompletion = "";

    main.run();

    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("https://api1.test.com/"));
  }

  @Test public void completeSingleEndpoint() throws Throwable {
    main.urlCompletion = "https://api1.test.co";

    main.run();

    assertEquals(Lists.newArrayList(
        "https://api1.test.com/account.json https://api1.test.com/users.json https://api1.test.com/usersList.json"),
        output.stdout);
  }

  @Test public void completeEndpointShortCommand1() throws Throwable {
    main.urlCompletion = "";
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("src/test/resources/commands/testcommand");

    main.run();

    assertEquals(Lists.newArrayList("/account.json /users.json /usersList.json"), output.stdout);
  }

  @Test public void completeEndpointShortCommand2() throws Throwable {
    main.urlCompletion = "/users";
    main.commandName = "okapi";
    main.arguments = Lists.newArrayList("src/test/resources/commands/testcommand");

    main.run();

    assertEquals(Lists.newArrayList("/users.json /usersList.json"), output.stdout);
  }
}
