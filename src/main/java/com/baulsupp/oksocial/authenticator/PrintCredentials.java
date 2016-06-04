package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.ServiceDefinition;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PrintCredentials {
  public static <T> void printKnownCredentials(OkHttpClient client, Request.Builder requestBuilder,
      AuthInterceptor<T> a) {
    Optional<T> credentials = a.readCredentials();
    ServiceDefinition<T> sd = a.credentialsStore().getServiceDefinition();

    Optional<String> credentialsString = credentials.map(sd::formatCredentialsString);

    try {
      Optional<ValidatedCredentials> validated =
          a.validate(client, requestBuilder).get(5, TimeUnit.SECONDS);

      printSuccess(sd, credentialsString, validated);
    } catch (IOException | InterruptedException | TimeoutException e) {
      printFailed(sd, credentialsString, e);
    } catch (ExecutionException e) {
      printFailed(sd, credentialsString, e.getCause());
    }
  }

  private static <T> void printSuccess(ServiceDefinition<T> sd, Optional<String> credentialsString,
      Optional<ValidatedCredentials> validated) {
    System.out.format("%-20s\t%20s\t%20s\n\t%s\n", sd.serviceName(),
        validated.flatMap(v -> v.username).orElse("-"),
        validated.flatMap(v -> v.clientName).orElse("-"), credentialsString.orElse("-"));
  }

  private static <T> void printFailed(ServiceDefinition<T> sd, Optional<String> credentialsString,
      Throwable e) {
    System.out.format("%-20s\t%s\n\t%s\n", sd.serviceName(),
        e.toString(), credentialsString.orElse("-"));
  }

  public static void printKnownCredentials(OkHttpClient client,
      Request.Builder requestBuilder, Iterable<AuthInterceptor<?>> services) {
    for (AuthInterceptor a : services) {
      PrintCredentials.printKnownCredentials(client, requestBuilder, a);
    }
  }
}
