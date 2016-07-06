package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SquareUpTest {

  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCompletionCache completionCache = new TestCompletionCache();

  {
    main.outputHandler = output;
    main.completionCache = completionCache;
  }

  @Test public void completeEndpointWithReplacements() throws Throwable {
    main.urlCompletion = "https://connect.squareup.com/";
    completionCache.store("squareup", "locations", Lists.newArrayList("AA", "bb"));

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("/v2/locations/AA/transactions"));
  }
}
