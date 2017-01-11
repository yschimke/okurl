package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.i9n.TestOutputHandler;
import java.io.IOException;
import okhttp3.OkHttpClient;
import org.junit.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class DiscoveryApiDocPresenterTest {
  private TestOutputHandler outputHandler = new TestOutputHandler();
  private OkHttpClient client = new OkHttpClient();

  @Test public void testExplainsUrl() throws IOException {
    assumeHasNetwork();

    DiscoveryApiDocPresenter p = new DiscoveryApiDocPresenter();
    p.explainApi("https://people.googleapis.com/v1/{+resourceName}", outputHandler, client);

    assertEquals(newArrayList("API: Google People API",
        "Documentation: https://developers.google.com/people/"), outputHandler.stdout);
  }
}
