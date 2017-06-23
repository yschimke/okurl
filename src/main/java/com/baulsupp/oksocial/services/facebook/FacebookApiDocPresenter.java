package com.baulsupp.oksocial.services.facebook;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.google.common.base.Throwables;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import static java.util.stream.Collectors.joining;

public class FacebookApiDocPresenter implements ApiDocPresenter {
  private ServiceDefinition<Oauth2Token> sd;

  public FacebookApiDocPresenter(ServiceDefinition<Oauth2Token> sd) {
    this.sd = sd;
  }

  @Override public void explainApi(String url, OutputHandler outputHandler, OkHttpClient client)
      throws IOException {
    outputHandler.info("service: " + sd.shortName());
    outputHandler.info("name: " + sd.serviceName());
    sd.apiDocs().ifPresent(d -> outputHandler.info("docs: " + d));
    sd.accountsLink().ifPresent(d -> outputHandler.info("apps: " + d));

    try {
      FacebookMetadata md = FacebookUtil.getMetadata(client, HttpUrl.parse(url)).get();
      outputHandler.info("");
      outputHandler.info("fields: " + md.fieldNames().stream().collect(joining(",")));
      outputHandler.info("");
      outputHandler.info("connections: " + md.connections().stream().collect(joining(",")));
    } catch (InterruptedException e) {
      throw (InterruptedIOException) new InterruptedIOException().initCause(e);
    } catch (ExecutionException e) {
      Throwables.propagateIfPossible(e.getCause(), IOException.class);
      throw Throwables.propagate(e.getCause());
    }
  }
}
