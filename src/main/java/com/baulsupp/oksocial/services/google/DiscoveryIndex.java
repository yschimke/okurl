package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.util.JsonUtil;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * API URL -> Discovery URL
 */
public class DiscoveryIndex {
  private final Map<String, List<String>> map;

  public DiscoveryIndex(Map<String, List<String>> map) {
    this.map = map;
  }

  public static DiscoveryIndex loadStatic() throws IOException {
    URL url = DiscoveryIndex.class.getResource("index.json");

    String definition = Resources.toString(url, StandardCharsets.UTF_8);

    return DiscoveryIndex.parse(definition);
  }

  public static DiscoveryIndex parse(String definition) throws IOException {
    Map m = JsonUtil.map(definition);
    return new DiscoveryIndex(m);
  }

  /**
   * Exact search
   */
  public List<String> getDiscoveryUrlForApi(String api) {
    return Optional.ofNullable(map.get(api)).orElse(Lists.newArrayList());
  }

  /**
   * Prefix search (returns longest)
   */
  public List<String> getDiscoveryUrlForPrefix(String prefix) {
    return map.entrySet()
        .stream()
        .filter(s1 -> match(prefix, s1.getKey()))
        .flatMap(s -> s.getValue().stream())
        .collect(toList());
  }

  boolean match(String prefix, String indexKey) {
    return indexKey.startsWith(prefix) || prefix.startsWith(indexKey);
  }
}
