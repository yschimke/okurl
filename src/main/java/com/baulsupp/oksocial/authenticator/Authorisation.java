package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import com.baulsupp.oksocial.util.UsageException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.OkHttpClient;

import static java.util.stream.Collectors.joining;

public class Authorisation {
  private ServiceInterceptor interceptor;
  private CredentialsStore credentialsStore;
  private OkHttpClient client;
  private OutputHandler outputHandler;

  public Authorisation(ServiceInterceptor interceptor, CredentialsStore credentialsStore,
      OkHttpClient client, OutputHandler outputHandler) {
    this.interceptor = interceptor;
    this.credentialsStore = credentialsStore;
    this.client = client;
    this.outputHandler = outputHandler;
  }

  public void authorize(Optional<AuthInterceptor<?>> auth, Optional<String> token,
      List<String> authArguments) throws Exception {
    failIfNoAuthInterceptor(auth.isPresent());

    if (token.isPresent()) {
      storeCredentials(auth.get(), token.get());
    } else {
      authRequest(auth.get(), authArguments);
    }
  }

  private void failIfNoAuthInterceptor(boolean present) {
    if (!present) {
      throw new UsageException(
          "unable to find authenticator. Specify name from " + interceptor.names()
              .stream()
              .collect(joining(", ")));
    }
  }

  private <T> void storeCredentials(AuthInterceptor<T> auth, String token) {
    T credentials = auth.serviceDefinition().parseCredentialsString(token);
    credentialsStore.storeCredentials(credentials, auth.serviceDefinition());
  }

  private <T> void authRequest(AuthInterceptor<T> auth, List<String> authArguments)
      throws Exception {

    T credentials = auth.authorize(client, outputHandler, authArguments);

    credentialsStore.storeCredentials(credentials, auth.serviceDefinition());

    Secrets.instance().saveIfNeeded();

    // TODO validate credentials
  }

  public <T> void renew(Optional<AuthInterceptor<T>> auth) throws IOException {
    failIfNoAuthInterceptor(auth.isPresent());

    ServiceDefinition<T> serviceDefinition = auth.get().serviceDefinition();
    Optional<T> credentials = credentialsStore.readDefaultCredentials(serviceDefinition);

    if (!credentials.isPresent()) {
      throw new UsageException("no existing credentials");
    }

    if (!auth.get().canRenew(credentials.get())) {
      throw new UsageException("credentials not renewable");
    }

    Optional<T> newCredentials = auth.get().renew(client, credentials.get());

    if (newCredentials.isPresent()) {
      credentialsStore.storeCredentials(newCredentials.get(), serviceDefinition);
    } else {
      throw new UsageException("failed to renew");
    }
  }
}
