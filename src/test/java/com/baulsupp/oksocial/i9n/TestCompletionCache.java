package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.completion.CompletionCache;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestCompletionCache implements CompletionCache {
  private Map<String, List<String>> cache = Maps.newConcurrentMap();

  @Override public Optional<List<String>> get(String service, String key, boolean freshOnly)
      throws IOException {
    return Optional.ofNullable(cache.get(service + "-" + key));
  }

  @Override public void store(String service, String key, List<String> values) throws IOException {
    cache.put(service + "-" + key, values);
  }
}
