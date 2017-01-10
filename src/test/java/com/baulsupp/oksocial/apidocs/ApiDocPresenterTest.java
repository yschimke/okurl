package com.baulsupp.oksocial.apidocs;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.i9n.TestOutputHandler;
import com.baulsupp.oksocial.services.test.TestAuthInterceptor;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class ApiDocPresenterTest {
  private List<AuthInterceptor<?>> services = newArrayList(new TestAuthInterceptor());
  private TestOutputHandler outputHandler = new TestOutputHandler();
  private ApiDocPresenter presenter =
      new ApiDocPresenter(services, new OkHttpClient(), CredentialsStore.NONE,
          outputHandler);

  @Test public void returnsAllUrls() throws IOException {
    presenter.explainApi("https://api1.test.com/me");

    assertEquals(newArrayList("https://api1.test.com/me"), outputHandler.stdout);
  }
}
