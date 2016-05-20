package com.baulsupp.oksocial.authenticator;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

// TODO verify credentials against live service and show username/client type etc
public class PrintCredentials {
  public static <T> void printKnownCredentials(AuthInterceptor<T> a) {
    T credentials = a.credentials();
    ServiceDefinition<T> sd = a.credentialsStore().getServiceDefinition();
    String credentialsString =
        credentials != null ? sd.formatCredentialsString(credentials) : "None";
    System.out.println(sd.serviceName() + " " + credentialsString);
  }

  public static void printKnownCredentials(Iterable<AuthInterceptor<?>> services) {
    for (AuthInterceptor a : services) {
      PrintCredentials.printKnownCredentials(a);
    }
  }
}
