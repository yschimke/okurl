package com.baulsupp.oksocial.services.google;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DiscoveryEndpoint {
  private final String baseUrl;
  private final Map<String, Object> map;

  public DiscoveryEndpoint(String baseUrl, Map<String, Object> map) {
    this.baseUrl = baseUrl;
    this.map = map;
  }

  public String id() {
    return (String) getRequired("id");
  }

  public String url() {
    return baseUrl + path();
  }

  private String path() {
    return (String) getRequired("path");
  }

  private Object getRequired(String name) {
    if (!map.containsKey(name)) {
      throw new NullPointerException("path not found");
    }

    return map.get(name);
  }

  public String description() {
    return (String) getRequired("description");
  }

  public List<String> scopeNames() {
    List<String> scopes = (List<String>) map.get("scopes");

    if (scopes != null) {
      return scopes;
    }

    return Lists.newArrayList();
  }

  public List<DiscoveryParameter> parameters() {
    Map<String, Map<String, Object>> o = (Map<String, Map<String, Object>>) map.get("parameters");

    if (o == null) {
      return Lists.newArrayList();
    }

    return o.entrySet()
        .stream()
        .map(p -> new DiscoveryParameter(p.getKey(), p.getValue()))
        .collect(toList());
  }
}
