package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.services.squareup.SquareUpAuthInterceptor;
import com.google.common.collect.Lists;
import com.baulsupp.oksocial.output.TestOutputHandler;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SquareUpTest {

  private Main main = new Main();
  private TestOutputHandler<Response> output = new TestOutputHandler<Response>();
  private TestCompletionVariableCache completionCache = new TestCompletionVariableCache();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.completionVariableCache = completionCache;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEndpointWithReplacements() throws Throwable {
    main.arguments = newArrayList("https://connect.squareup.com/");
    main.urlComplete = true;
    completionCache.store("squareup", "locations", Lists.newArrayList("AA", "bb"));
    credentialsStore.storeCredentials(new Oauth2Token(""),
        new SquareUpAuthInterceptor().serviceDefinition());

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("/v2/locations/AA/transactions"));
  }
}
