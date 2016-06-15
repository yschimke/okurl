package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.Optional.empty;

public class PrintCredentials {
  private OkHttpClient client;
  private CredentialsStore credentialsStore;

  public PrintCredentials(OkHttpClient client, CredentialsStore credentialsStore) {
    this.client = client;
    this.credentialsStore = credentialsStore;
  }

  public <T> void printKnownCredentials(Request.Builder requestBuilder,
      AuthInterceptor<T> a) {
    ServiceDefinition<T> sd = a.serviceDefinition();
    Optional<T> credentials = credentialsStore.readDefaultCredentials(sd);

    Optional<String> credentialsString = credentials.map(sd::formatCredentialsString);

    if (credentials.isPresent()) {
      try {
        Optional<ValidatedCredentials> validated =
            a.validate(client, requestBuilder, credentials.get()).get(5, TimeUnit.SECONDS);

        printSuccess(sd, credentialsString, validated);
      } catch (IOException | InterruptedException | TimeoutException e) {
        printFailed(sd, credentialsString, e);
      } catch (ExecutionException e) {
        printFailed(sd, credentialsString, e.getCause());
      }
    } else {
      printSuccess(sd, empty(), empty());
    }
  }

  private <T> void printSuccess(ServiceDefinition<T> sd, Optional<String> credentialsString,
      Optional<ValidatedCredentials> validated) {
    System.out.format("%-20s\t%20s\t%20s\n\t%s\n", sd.serviceName(),
        validated.flatMap(v -> v.username).orElse("-"),
        validated.flatMap(v -> v.clientName).orElse("-"), credentialsString.orElse("-"));
  }

  private <T> void printFailed(ServiceDefinition<T> sd, Optional<String> credentialsString,
      Throwable e) {
    System.out.format("%-20s\t%s\n\t%s\n", sd.serviceName(),
        e.toString(), credentialsString.orElse("-"));
  }

  public void printKnownCredentials(Request.Builder requestBuilder,
      Iterable<AuthInterceptor<?>> services) {
    for (AuthInterceptor a : services) {
      printKnownCredentials(requestBuilder, a);
    }
  }
}
