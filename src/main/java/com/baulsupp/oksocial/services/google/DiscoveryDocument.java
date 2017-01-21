package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.util.JsonUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * https://developers.google.com/discovery/v1/using
 */
public class DiscoveryDocument {
  private final Map<String, Object> map;

  public DiscoveryDocument(Map<String, Object> map) {
    this.map = map;
  }

  public static DiscoveryDocument parse(String definition) throws IOException {
    return new DiscoveryDocument(JsonUtil.map(definition));
  }

  public String getBaseUrl() {
    return "" + map.get("rootUrl") + map.get("servicePath");
  }

  public List<String> getUrls() {
    return getEndpoints()
        .stream()
        .map(e -> e.url())
        .distinct()
        .collect(toList());
  }

  public List<DiscoveryEndpoint> getEndpoints() {
    String prefix = "";

    return expandEndpoints(prefix, map);
  }

  private List<DiscoveryEndpoint> expandEndpoints(Map<String, Object> map) {
    Map<String, Map<String, Object>> resources = getResources(map);

    List<DiscoveryEndpoint> thisLevel = resources.values()
        .stream()
        .flatMap(r -> {
          return getMethods(r).values().stream();
        })
        .map(m -> new DiscoveryEndpoint(getBaseUrl(), m))
        .collect(toList());

    return thisLevel;
  }

  private Map<String, Map<String, Object>> getResources(Map<String, Object> map) {
    if (!map.containsKey("resources")) {
      return Collections.emptyMap();
    }

    //noinspection unchecked
    return (Map<String, Map<String, Object>>) map.get("resources");
  }

  private Map<String, Map<String, Object>> getMethods(Map<String, Object> resource) {
    if (!resource.containsKey("methods")) {
      return Collections.emptyMap();
    }

    //noinspection unchecked
    return (Map<String, Map<String, Object>>) resource.get("methods");
  }

  public String getApiName() {
    return (String) map.get("title");
  }

  public String getDocLink() {
    return (String) map.get("documentationLink");
  }

  public Optional<DiscoveryEndpoint> findEndpoint(String url) {
    return getEndpoints().stream().filter(e -> matches(url, e)).findAny();
  }

  private boolean matches(String url, DiscoveryEndpoint e) {
    return e.url().equals(url) || e.matches(url);
  }
}