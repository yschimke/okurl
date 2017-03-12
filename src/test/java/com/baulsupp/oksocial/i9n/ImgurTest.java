package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import ee.schimke.oksocial.output.TestOutputHandler;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImgurTest {
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
    main.arguments = Lists.newArrayList("imgur");

    main.run();

    assertEquals("abc", credentialsStore.tokens.get("api.imgur.com"));
  }
}
