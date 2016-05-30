package com.baulsupp.oksocial.credentials;

import java.util.Optional;
import java.util.prefs.Preferences;

public class PreferencesCredentialsStore<T> implements CredentialsStore<T> {
  private ServiceDefinition<T> serviceDefinition;
  private Preferences userNode = Preferences.userNodeForPackage(this.getClass());

  public PreferencesCredentialsStore(ServiceDefinition<T> serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  public ServiceDefinition<T> getServiceDefinition() {
    return serviceDefinition;
  }

  @Override public Optional<T> readDefaultCredentials() {
    String credentialsString = userNode.get(tokenKey(), null);
    return Optional.ofNullable(credentialsString).map(s -> serviceDefinition.parseCredentialsString(s));
  }

  private String tokenKey() {
    return serviceDefinition.serviceName() + ".token";
  }

  @Override public void storeCredentials(T credentials) {
    String credentialsString = serviceDefinition.formatCredentialsString(credentials);
    userNode.put(tokenKey(), credentialsString);
  }
}
