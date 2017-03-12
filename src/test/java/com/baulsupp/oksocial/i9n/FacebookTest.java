package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.services.facebook.FacebookApiDocPresenter;
import com.baulsupp.oksocial.services.facebook.FacebookAuthInterceptor;
import com.google.common.collect.Lists;
import ee.schimke.oksocial.output.TestOutputHandler;
import java.io.IOException;
import java.util.List;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.baulsupp.oksocial.util.TestUtil.assumeHasNetwork;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FacebookTest {
  private Main main = new Main();
  private TestOutputHandler<Response> output = new TestOutputHandler<Response>();

  {
    main.outputHandler = output;
    main.credentialsStore = new TestCredentialsStore();
  }

  private ServiceDefinition<Oauth2Token> sd = new FacebookAuthInterceptor().serviceDefinition();
  private FacebookApiDocPresenter p;

  @BeforeEach
  public void loadPresenter() throws IOException {
    p = new FacebookApiDocPresenter(sd);
  }

  @Test public void testExplainsUrl() throws IOException {
    assumeHasNetwork();

    main.arguments = Lists.newArrayList("https://graph.facebook.com/v2.8/app/groups");
    main.apiDoc = true;

    main.run();

    List<String> es = newArrayList("service: facebook", "name: Facebook API",
        "docs: https://developers.facebook.com/docs/graph-api",
        "apps: https://developers.facebook.com/apps/");

    assertEquals(es, output.stdout);
  }
}
