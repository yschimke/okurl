package com.baulsupp.oksocial.completion;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public interface CompletionVariableCache {
  CompletionVariableCache NONE = new CompletionVariableCache() {
    @Override public Optional<List<String>> get(String service, String key) {
      return Optional.empty();
    }

    @Override public void store(String service, String key, List<String> values) {
    }
  };

  Optional<List<String>> get(String service, String key);

  void store(String service, String key, List<String> values);

  default CompletableFuture<List<String>> compute(String service, String key,
      Supplier<CompletableFuture<List<String>>> s) {
    Optional<List<String>> values = get(service, key);

    if (values.isPresent()) {
      return completedFuture(values.get());
    } else {
      CompletableFuture<List<String>> x = s.get();
      x.thenAccept(l -> store(service, key, l));
      return x;
    }
  }
}
