package com.baulsupp.oksocial.services.google;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

  public boolean matches(String requestUrl) {
    if (!requestUrl.startsWith(baseUrl)) {
      return false;
    }

    String requestUrlPath = requestUrl.substring(baseUrl.length());

    return buildDocPathRegex().matcher(requestUrlPath).matches();
  }

  private Pattern buildDocPathRegex() {
    List<DiscoveryParameter> parameters = parameters();

    boolean hasQueryParams = false;

    String pathPattern = this.path();

    for (DiscoveryParameter p : parameters) {
      if (p.location().equals("path")) {
        String pPattern = p.pattern();
        if (pPattern == null) {
          pPattern = ".*";
        } else if (pPattern.matches("\\^.*\\$")) {
          pPattern = pPattern.substring(1, pPattern.length() - 1);
        }
        String x = "\\{\\+?" + p.name() + "\\}";
        pathPattern = pathPattern.replaceAll(x, pPattern);
      } else if (p.location().equals("query")) {
        hasQueryParams = true;
      }
    }

    return Pattern.compile(pathPattern + (hasQueryParams ? "(\\?.*)?" : ""));
  }

  public String httpMethod() {
    return (String) map.getOrDefault("httpMethod", "GET");
  }
}
