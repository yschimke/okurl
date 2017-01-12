package com.baulsupp.oksocial.util;

import com.google.common.base.Throwables;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class FutureUtil {
  public static <T> CompletableFuture<List<T>> join(List<CompletableFuture<T>> futures) {
    CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);

    return CompletableFuture.allOf(cfs)
        .thenApply(v -> futures.stream().
            map(CompletableFuture::join).
            collect(Collectors.toList())
        );
  }

  public static <U> U ioSafeGet(Future<U> load) {
    try {
      return load.get();
    } catch (InterruptedException e) {
      throw Throwables.propagate(e);
    } catch (ExecutionException e) {
      throw Throwables.propagate(e.getCause());
    }
  }
}
