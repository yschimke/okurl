package com.baulsupp.oksocial.services.facebook;

import java.util.Map;
import java.util.Set;

public class FacebookMetadata {
  private Map<String, Object> metadata;

  public FacebookMetadata(Map<String, Object> metadata) {

    this.metadata = metadata;
  }

  public Set<String> connections() {
    return ((Map<String, Object>) metadata.get("connections")).keySet();
  }
}
