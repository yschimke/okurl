package com.baulsupp.oksocial.completion;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.credentials.CredentialsStore;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class UrlCompleter {
  private static Logger logger = Logger.getLogger(UrlCompleter.class.getName());

  private Iterable<AuthInterceptor<?>> services;
  private OkHttpClient client;
  private CredentialsStore credentialsStore;
  private CompletionCache completionCache;
  private Clock clock = Clock.systemDefaultZone();

  public UrlCompleter(List<AuthInterceptor<?>> services, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionCache completionCache) {
    this.services = services;
    this.client = client;
    this.credentialsStore = credentialsStore;
    this.completionCache = completionCache;
  }

  public List<String> urlList(String prefix) throws IOException {
    return endpointUrls(prefix);
  }

  private List<String> endpointUrls(String prefix) throws IOException {
    long to = clock.millis() + 2000;

    List<String> results = Lists.newArrayList();

    List<Future<List<String>>> futures = Lists.newArrayList();

    Optional<HttpUrl> fullUrl = parseUrl(prefix);

    if (fullUrl.isPresent()) {
      for (AuthInterceptor<?> a : services) {
        if (a.supportsUrl(fullUrl.get())) {
          futures.add(a.matchingUrls(prefix, client, credentialsStore, completionCache, true));
        }
      }
    } else {
      for (AuthInterceptor<?> a : services) {
        futures.add(a.matchingUrls(prefix, client, credentialsStore, completionCache, false));
      }
    }

    for (Future<List<String>> f : futures) {
      try {
        List<String> result = f.get(to - clock.millis(), TimeUnit.MILLISECONDS);

        results.addAll(result);
      } catch (ExecutionException e) {
        logger.log(Level.WARNING, "failure during url completion", e);
      } catch (InterruptedException | TimeoutException e) {
        logger.log(Level.FINE, "timeout during url completion", e);
      }
    }

    return results;
  }

  private Optional<HttpUrl> parseUrl(String prefix) {
    if (isSingleApi(prefix)) {
      return Optional.ofNullable(HttpUrl.parse(prefix));
    } else {
      return Optional.empty();
    }
  }

  private boolean isSingleApi(String prefix) {
    return prefix.matches("https://[^/]+/.*");
  }
}
