package com.baulsupp.oksocial.apidocs;

import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.google.common.base.Throwables;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Optional;
import okhttp3.OkHttpClient;

public class ServiceApiDocPresenter implements ApiDocPresenter {
  private final ServiceInterceptor services;
  private final OkHttpClient client;
  private final CredentialsStore credentialsStore;

  public ServiceApiDocPresenter(ServiceInterceptor services, OkHttpClient client,
      CredentialsStore credentialsStore) {
    this.services = services;
    this.client = client;
    this.credentialsStore = credentialsStore;
  }

  public void explainApi(String url, OutputHandler outputHandler, OkHttpClient client)
      throws IOException {
    Optional<ApiDocPresenter> presenter =
        services.getByUrl(url).map(s -> {
          try {
            return s.apiDocPresenter(url);
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        });

    if (presenter.isPresent()) {
      presenter.get().explainApi(url, outputHandler, client);
    } else {
      outputHandler.info("No documentation for: " + url);
    }
  }
}
