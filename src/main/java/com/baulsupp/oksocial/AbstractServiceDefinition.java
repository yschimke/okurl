package com.baulsupp.oksocial;

import com.baulsupp.oksocial.credentials.ServiceDefinition;

public abstract class AbstractServiceDefinition<T> implements ServiceDefinition<T> {
  private String apiHost;
  private String serviceName;
  private final String shortName;

  public AbstractServiceDefinition(String apiHost, String serviceName, String shortName) {
    this.apiHost = apiHost;
    this.serviceName = serviceName;
    this.shortName = shortName;
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
}
