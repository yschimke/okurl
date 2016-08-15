package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.services.sheetsu.SheetsuAuthInterceptor;
import com.baulsupp.oksocial.services.squareup.SquareUpAuthInterceptor;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SheetsuTest {

  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCompletionVariableCache completionCache = new TestCompletionVariableCache();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.completionVariableCache = completionCache;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEndpointWithReplacements() throws Throwable {
    main.urlCompletion = "https://sheetsu.com/";
    credentialsStore.storeCredentials(new BasicCredentials("", ""),
        new SheetsuAuthInterceptor().serviceDefinition());

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).startsWith("https://sheetsu.com/apis/v1.0/"));
  }
}
