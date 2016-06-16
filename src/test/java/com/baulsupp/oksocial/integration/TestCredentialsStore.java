package com.baulsupp.oksocial.integration;

import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.baulsupp.oksocial.credentials.ServiceDefinition;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class TestCredentialsStore implements CredentialsStore {
  public Map<String, String> tokens = Maps.newHashMap();

  @Override public <T> Optional<T> readDefaultCredentials(ServiceDefinition<T> serviceDefinition) {
    return ofNullable(tokens.get(serviceDefinition.apiHost()))
        .map(serviceDefinition::parseCredentialsString);
  }

  @Override
  public <T> void storeCredentials(T credentials, ServiceDefinition<T> serviceDefinition) {
    tokens.put(serviceDefinition.apiHost(), serviceDefinition.formatCredentialsString(credentials));
  }
}
