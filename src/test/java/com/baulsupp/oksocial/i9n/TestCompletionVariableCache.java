package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.completion.CompletionVariableCache;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestCompletionVariableCache implements CompletionVariableCache {
  private Map<String, List<String>> cache = Maps.newConcurrentMap();

  @Override public Optional<List<String>> get(String service, String key) {
    return Optional.ofNullable(cache.get(service + "-" + key));
  }

  @Override public void store(String service, String key, List<String> values) {
    cache.put(service + "-" + key, values);
  }
}
