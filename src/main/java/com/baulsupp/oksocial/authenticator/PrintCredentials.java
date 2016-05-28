package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.ServiceDefinition;
import java.io.IOException;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PrintCredentials {
  public static <T> void printKnownCredentials(OkHttpClient client, Request.Builder requestBuilder,
      AuthInterceptor<T> a) {
    T credentials = a.credentials();
    ServiceDefinition<T> sd = a.credentialsStore().getServiceDefinition();

    Optional<String> credentialsString =
        Optional.ofNullable(a.credentials()).map(s -> sd.formatCredentialsString(credentials));

    Optional<ValidatedCredentials> validated = null;
    try {
      validated = a.validate(client, requestBuilder);
    } catch (IOException e) {
      System.err.println(e.toString());
      validated = Optional.empty();
    }

    System.out.format("%-20s\t%20s\t%20s\n\t%s\n", sd.serviceName(),
        validated.flatMap(v -> v.username).orElse("-"),
        validated.flatMap(v -> v.clientName).orElse("-"), credentialsString.orElse("-"));
  }

  public static void printKnownCredentials(OkHttpClient client,
      Request.Builder requestBuilder, Iterable<AuthInterceptor<?>> services) {
    for (AuthInterceptor a : services) {
      PrintCredentials.printKnownCredentials(client, requestBuilder, a);
    }
  }
}
