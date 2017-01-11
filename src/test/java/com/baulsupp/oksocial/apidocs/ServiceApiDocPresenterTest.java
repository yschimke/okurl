package com.baulsupp.oksocial.apidocs;

import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.i9n.TestOutputHandler;
import java.io.IOException;
import okhttp3.OkHttpClient;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class ServiceApiDocPresenterTest {
  private TestOutputHandler outputHandler = new TestOutputHandler();
  private OkHttpClient client = new OkHttpClient();
  private CredentialsStore credentialsStore = CredentialsStore.NONE;
  private ServiceApiDocPresenter presenter =
      new ServiceApiDocPresenter(new ServiceInterceptor(client, credentialsStore), client,
          credentialsStore);

  @Test public void returnsAllUrls() throws IOException {
    presenter.explainApi("https://api1.test.com/me", outputHandler, client);

    assertEquals(newArrayList("Test: https://api1.test.com/me"), outputHandler.stdout);
  }

  @Test public void errorForUnknown() throws IOException {
    presenter.explainApi("https://api1.blah.com/me", outputHandler, client);

    assertEquals(newArrayList("No documentation for: https://api1.blah.com/me"),
        outputHandler.stdout);
  }
}
