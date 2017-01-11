package com.baulsupp.oksocial;

import com.baulsupp.oksocial.credentials.ServiceDefinition;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class AbstractServiceDefinition<T> implements ServiceDefinition<T> {
  private final String apiHost;
  private final String serviceName;
  private final String shortName;
  private final String apiDocs;

  public AbstractServiceDefinition(String apiHost, String serviceName, String shortName,
      String apiDocs) {
    this.apiHost = apiHost;
    this.serviceName = serviceName;
    this.shortName = shortName;
    this.apiDocs = apiDocs;
  }

  @Override public String shortName() {
    return shortName;
  }

  @Override public String apiHost() {
    return apiHost;
  }

  @Override public String serviceName() {
    return serviceName;
  }

  @Override public Optional<String> apiDocs() {
    return ofNullable(apiDocs);
  }
}
