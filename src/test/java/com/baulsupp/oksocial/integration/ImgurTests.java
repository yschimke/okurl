package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImgurTests {
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
    main.arguments = Lists.newArrayList("imgur");

    main.run();

    assertEquals("abc", credentialsStore.tokens.get("api.imgur.com"));
  }
}
