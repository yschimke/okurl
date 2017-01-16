package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import okhttp3.OkHttpClient;

import static com.baulsupp.oksocial.util.FutureUtil.ioSafeGet;
import static com.baulsupp.oksocial.util.FutureUtil.join;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DiscoveryApiDocPresenter implements ApiDocPresenter {
  @Override public void explainApi(String url, OutputHandler outputHandler, OkHttpClient client)
      throws IOException {
    DiscoveryIndex discoveryIndex = DiscoveryIndex.loadStatic();
    List<String> discoveryPaths = discoveryIndex.getDiscoveryUrlForPrefix(url);

    DiscoveryRegistry registry = DiscoveryRegistry.instance(client);

    CompletableFuture<List<DiscoveryDocument>> docs =
        join(discoveryPaths.stream().map(p -> registry.load(p)).collect(toList()));

    Optional<DiscoveryDocument> bestDoc =
        ioSafeGet(docs.thenApply(d -> {
          Optional<DiscoveryDocument> exactMatch =
              d.stream().filter(x -> matches(url, x)).findFirst();

          if (exactMatch.isPresent()) {
            return exactMatch;
          }

          // requested url may be a substring of longest baseUrl
          // assume that this means that single unique service owns this base url
          Optional<DiscoveryDocument> best = d.stream()
              .filter(service -> url.startsWith(service.getBaseUrl()))
              .max(Comparator.comparing(dd -> dd.getBaseUrl().length()));

          if (best.isPresent()) {
            return best;
          }

          // multiple services sharing baseurl - return first
          outputHandler.info("Multiple services for path " + url);
          return Optional.empty();
        }));

    if (bestDoc.isPresent()) {
      DiscoveryDocument s = bestDoc.get();
      outputHandler.info("name: " + s.getApiName());
      outputHandler.info("docs: " + s.getDocLink());

      Optional<DiscoveryEndpoint> e = s.findEndpoint(url);

      e.ifPresent(de -> {
        outputHandler.info("endpoint id: " + de.id());
        outputHandler.info("url: " + de.url());
        outputHandler.info("scopes: " + de.scopeNames().stream().collect(joining(", ")));
        outputHandler.info("");
        outputHandler.info(de.description());
        outputHandler.info("");
        de.parameters().forEach(p -> {
          outputHandler.info("parameter: " + p.name() + " (" + p.type() + ")");
          outputHandler.info(p.description());
        });
      });

      if (!e.isPresent()) {
        outputHandler.info("base: " + s.getBaseUrl());
      }
    } else {
      outputHandler.info("No specific API found");
      outputHandler.info("https://developers.google.com/apis-explorer/#p/");
    }
  }

  private boolean matches(String url, DiscoveryDocument x) {
    List<DiscoveryEndpoint> eps = x.getEndpoints();

    for (DiscoveryEndpoint ep : eps) {
      if (ep.matches(url)) {
        return true;
      }
    }

    return false;
  }
}
