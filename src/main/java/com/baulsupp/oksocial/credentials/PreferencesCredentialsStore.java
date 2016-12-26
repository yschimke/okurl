package com.baulsupp.oksocial.credentials;

import java.util.Optional;
import java.util.prefs.Preferences;

public class PreferencesCredentialsStore implements CredentialsStore {
  private final Optional<String> tokenSet;
  private Preferences userNode = Preferences.userNodeForPackage(this.getClass());

  public PreferencesCredentialsStore(Optional<String> tokenSet) {
    this.tokenSet = tokenSet;
  }

  @Override public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
    String credentialsString = userNode.get(tokenKey(serviceDefinition.apiHost()), null);
    return Optional.ofNullable(credentialsString)
        .map(serviceDefinition::parseCredentialsString);
  }

  private String tokenKey(String name) {
    return name + ".token" + tokenSet.map(s -> "." + s).orElse("");
  }

  @Override
  public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    String credentialsString = serviceDefinition.formatCredentialsString(credentials);
    userNode.put(tokenKey(serviceDefinition.apiHost()), credentialsString);
  }
}
