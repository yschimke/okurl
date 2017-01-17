package com.baulsupp.oksocial.services.facebook;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class FacebookMetadata {
  private Map<String, Object> metadata;

  public FacebookMetadata(Map<String, Object> metadata) {
    this.metadata = metadata == null ? Maps.newHashMap() : metadata;
  }

  public Set<String> connections() {
    Map<String, Object> connections = ((Map<String, Object>) metadata.get("connections"));
    return connections == null ? Sets.newHashSet() : connections.keySet();
  }

  public List<Map<String, String>> fields() {
    List<Map<String, String>> fields = (List<Map<String, String>>) metadata.get("fields");
    return fields == null ? Lists.newArrayList() : fields;
  }

  public List<String> fieldNames() {
    return fields().stream().map(f -> f.get("name")).collect(toList());
  }
}
