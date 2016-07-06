package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CompletionCache {
  CompletionCache NONE = new CompletionCache() {
    @Override public Optional<List<String>> get(String service, String key, boolean freshOnly) {
      return Optional.empty();
    }

    @Override public void store(String service, String key, List<String> values) {
    }
  };

  Optional<List<String>> get(String service, String key, boolean freshOnly) throws IOException;

  void store(String service, String key, List<String> values) throws IOException;
}
