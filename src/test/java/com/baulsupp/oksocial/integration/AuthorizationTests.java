package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthorizationTests {
  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.credentialsStore = credentialsStore;
  }

  @Test public void setToken() throws Exception {
    main.authorize = true;
    main.token = "abc";
    main.arguments = Lists.newArrayList("test");

    main.run();

    assertEquals("abc", credentialsStore.tokens.get("localhost"));
  }

  @Test public void authorize() throws Exception {
    main.authorize = true;
    main.arguments = Lists.newArrayList("test");

    main.run();

    assertEquals("testToken", credentialsStore.tokens.get("localhost"));
  }

  @Test public void authorizeByHost() throws Exception {
    main.authorize = true;
    main.arguments = Lists.newArrayList("https://localhost/test");

    main.run();

    assertEquals("testToken", credentialsStore.tokens.get("localhost"));
  }

  @Test public void authorizeWithArgs() throws Exception {
    main.authorize = true;
    main.arguments = Lists.newArrayList("test", "TOKENARG");

    main.run();

    assertEquals("TOKENARG", credentialsStore.tokens.get("localhost"));
  }
}
