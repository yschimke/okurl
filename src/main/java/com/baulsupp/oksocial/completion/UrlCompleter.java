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

import static java.lang.Math.min;
import static java.util.regex.Pattern.quote;

public class UrlCompleter {
  private static Logger logger = Logger.getLogger(UrlCompleter.class.getName());

  private Iterable<AuthInterceptor<?>> services;
  private OkHttpClient client;
  private CredentialsStore credentialsStore;
  private CompletionVariableCache completionVariableCache;
  private Clock clock = Clock.systemDefaultZone();

  public UrlCompleter(List<AuthInterceptor<?>> services, OkHttpClient client,
      CredentialsStore credentialsStore, CompletionVariableCache completionVariableCache) {
    this.services = services;
    this.client = client;
    this.credentialsStore = credentialsStore;
    this.completionVariableCache = completionVariableCache;
  }

  public UrlList urlList(String prefix) throws IOException {

    Optional<HttpUrl> fullUrl = parseUrl(prefix);

    if (fullUrl.isPresent()) {
      HttpUrl u = fullUrl.get();

      for (AuthInterceptor<?> a : services) {
        if (a.supportsUrl(u)) {
          try {
            return a.apiCompleter(prefix, client, credentialsStore, completionVariableCache)
                .siteUrls(u)
                .get();
          } catch (InterruptedException e) {
            logger.log(Level.FINE, "interrupted", e);
          } catch (ExecutionException e) {
            logger.log(Level.WARNING, "completion failed", e.getCause());
          }
        }
      }

      // won't match anything
      return new UrlList("FAILED", Lists.newArrayList());
    } else {
      List<Future<UrlList>> futures = Lists.newArrayList();

      for (AuthInterceptor<?> a : services) {
        futures.add(
            a.apiCompleter("", client, credentialsStore, completionVariableCache).prefixUrls());
      }

      return futuresToList(prefix, futures);
    }
  }

  private UrlList futuresToList(String prefix, List<Future<UrlList>> futures) {
    long to = clock.millis() + 2000;

    List<String> results = Lists.newArrayList();

    for (Future<UrlList> f : futures) {
      try {
        UrlList result = f.get(to - clock.millis(), TimeUnit.MILLISECONDS);

        results.addAll(result.getUrls(prefix));
      } catch (ExecutionException e) {
        logger.log(Level.WARNING, "failure during url completion", e.getCause());
      } catch (InterruptedException | TimeoutException e) {
        logger.log(Level.FINE, "timeout during url completion", e);
      }
    }

    return new UrlList(quote(prefix), results);
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

  public static boolean isPossibleAddress(String urlCompletion) {
    return urlCompletion.startsWith("https://".substring(0, min(urlCompletion.length(), 8)));
  }
}
