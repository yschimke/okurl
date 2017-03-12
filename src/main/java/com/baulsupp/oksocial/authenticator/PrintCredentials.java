package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static ee.schimke.oksocial.output.util.FutureUtil.optionalStream;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

public class PrintCredentials {
  private final OutputHandler outputHandler;
  private final OkHttpClient client;
  private final CredentialsStore credentialsStore;
  private final ServiceInterceptor serviceInterceptor;

  public PrintCredentials(OkHttpClient client, CredentialsStore credentialsStore,
      OutputHandler outputHandler, ServiceInterceptor serviceInterceptor) {
    this.client = client;
    this.credentialsStore = credentialsStore;
    this.outputHandler = outputHandler;
    this.serviceInterceptor = serviceInterceptor;
  }

  public <T> void printKnownCredentials(Request.Builder requestBuilder, AuthInterceptor<T> a,
      boolean full) {
    ServiceDefinition<T> sd = a.serviceDefinition();
    Optional<T> credentials = credentialsStore.readDefaultCredentials(sd);

    Optional<String> credentialsString = credentials.map(sd::formatCredentialsString);

    if (credentials.isPresent()) {
      try {
        Optional<ValidatedCredentials> validated =
            a.validate(client, requestBuilder, credentials.get()).get(5, TimeUnit.SECONDS);

        printSuccess(sd, validated);
      } catch (IOException | InterruptedException | TimeoutException e) {
        printFailed(sd, e);
      } catch (ExecutionException e) {
        printFailed(sd, e.getCause());
      }
    } else {
      printSuccess(sd, empty());
    }
    if (full) {
      outputHandler.info(credentialsString.orElse("-"));
    }
  }

  private <T> void printSuccess(ServiceDefinition<T> sd, Optional<ValidatedCredentials> validated) {
    outputHandler.info(
        String.format("%-40s\t%-20s\t%-20s", sd.serviceName() + " (" + sd.shortName() + ")",
            validated.flatMap(v -> v.username).orElse("-"),
            validated.flatMap(v -> v.clientName).orElse("-")));
  }

  private <T> void printFailed(ServiceDefinition<T> sd,
      Throwable e) {
    outputHandler.info(String.format("%-20s	%s", sd.serviceName(), e.toString()));
  }

  public void showCredentials(List<String> arguments, Callable<Request.Builder> requestBuilder)
      throws Exception {
    Iterable<AuthInterceptor<?>> services = serviceInterceptor.services();

    boolean full = !arguments.isEmpty();

    if (!arguments.isEmpty()) {
      services = arguments.stream().flatMap(a ->
          optionalStream(serviceInterceptor.findAuthInterceptor(a))).collect(
          toList());
    }

    for (AuthInterceptor a : services) {
      printKnownCredentials(requestBuilder.call(), a, full);
    }
  }
}
