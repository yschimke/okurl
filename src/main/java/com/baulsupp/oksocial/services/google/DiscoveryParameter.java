package com.baulsupp.oksocial.services.google;

import java.util.Map;

public class DiscoveryParameter {
  private String name;
  private final Map<String, Object> map;

  public DiscoveryParameter(String name, Map<String, Object> map) {
    this.name = name;
    this.map = map;

  }

  public String name() {
    return name;
  }

  public String type() {
    return (String) map.get("type");
  }

  public String description() {
    return (String) map.get("description");
  }

  public String location() {
    return (String) map.get("location");
  }

  public String pattern() {
    return (String) map.get("pattern");
  }
}
