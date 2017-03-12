package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import ee.schimke.oksocial.output.TestOutputHandler;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthorizationTest {
  private Main main = new Main();
  private TestOutputHandler<Response> output = new TestOutputHandler<Response>();
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
    main.arguments = Lists.newArrayList("https://test.com/test");

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
