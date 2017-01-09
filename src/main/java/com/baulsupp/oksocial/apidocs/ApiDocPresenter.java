package com.baulsupp.oksocial.apidocs;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.output.OutputHandler;
import java.util.List;
import okhttp3.OkHttpClient;

public class ApiDocPresenter {
  private final List<AuthInterceptor<?>> services;
  private final OkHttpClient client;
  private final CredentialsStore credentialsStore;
  private OutputHandler outputHandler;

  public ApiDocPresenter(List<AuthInterceptor<?>> services, OkHttpClient client,
      CredentialsStore credentialsStore, OutputHandler outputHandler) {
    this.services = services;
    this.client = client;
    this.credentialsStore = credentialsStore;
    this.outputHandler = outputHandler;
  }

  public void explainApi(String u) {
    // TODO complete
    outputHandler.info(u);
  }
}
