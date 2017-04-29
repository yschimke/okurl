package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.google.common.util.concurrent.Futures;
import ee.schimke.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
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

  public <T> void printKnownCredentials(Future<Optional<ValidatedCredentials>> future,
      AuthInterceptor<T> a) {
    ServiceDefinition<T> sd = a.serviceDefinition();

    try {
      Optional<ValidatedCredentials> validated = future.get(5, TimeUnit.SECONDS);

      printSuccess(sd, validated);
    } catch (InterruptedException | TimeoutException e) {
      printFailed(sd, e);
    } catch (ExecutionException e) {
      printFailed(sd, e.getCause());
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
    if (e instanceof TimeoutException) {
      outputHandler.info(String.format("%-20s	%s", sd.serviceName(), "timeout"));
    } else if (e instanceof IOException) {
      outputHandler.info(String.format("%-20s	%s", sd.serviceName(), e.getCause()));
    } else {
      outputHandler.info(String.format("%-20s	%s", sd.serviceName(), e.toString()));
    }
  }

  public void showCredentials(List<String> arguments, Supplier<Request.Builder> requestBuilder)
      throws Exception {
    Iterable<AuthInterceptor<?>> services = serviceInterceptor.services();

    boolean full = !arguments.isEmpty();

    if (!arguments.isEmpty()) {
      services = arguments.stream().flatMap(a ->
          optionalStream(serviceInterceptor.findAuthInterceptor(a))).collect(
          toList());
    }

    Map<String, Future<Optional<ValidatedCredentials>>> futures =
        validate(services, requestBuilder);

    for (AuthInterceptor<?> service : services) {
      Future<Optional<ValidatedCredentials>> future = futures.get(service.name());

      if (future != null) {
        printKnownCredentials(future, service);
      } else {
        printSuccess(service.serviceDefinition(), empty());
      }
      if (full) {
        printCredentials(service);
      }
    }
  }

  private <T> void printCredentials(AuthInterceptor<T> service) {
    ServiceDefinition<T> sd = service.serviceDefinition();
    Optional<String> credentialsString = credentialsStore.readDefaultCredentials(
        sd).map(sd::formatCredentialsString);
    outputHandler.info(credentialsString.orElse("-"));
  }

  private Map<String, Future<Optional<ValidatedCredentials>>> validate(
      Iterable<AuthInterceptor<?>> services, Supplier<Request.Builder> requestBuilder) {
    Map<String, Future<Optional<ValidatedCredentials>>> result = new HashMap<>();

    for (AuthInterceptor<?> sv : services) {
      validate(requestBuilder, result, sv);
    }

    return result;
  }

  private <T> void validate(Supplier<Request.Builder> requestBuilder,
      Map<String, Future<Optional<ValidatedCredentials>>> result, AuthInterceptor<T> sv) {
    Optional<T> credentials = credentialsStore.readDefaultCredentials(sv.serviceDefinition());

    if (credentials.isPresent()) {
      try {
        Future<Optional<ValidatedCredentials>> future =
            sv.validate(client, requestBuilder.get(), credentials.get());
        result.put(sv.name(), future);
      } catch (IOException ioe) {
        result.put(sv.name(), Futures.immediateFailedFuture(ioe));
      }
    }
  }
}
