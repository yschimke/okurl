package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.util.Util;
import com.google.common.base.Strings;
import java.io.IOException;
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

    DiscoveryRegistry registry = DiscoveryRegistry.loadStatic();

    CompletableFuture<List<DiscoveryDocument>> docs =
        join(discoveryPaths.stream().map(p -> registry.load(client, p)).collect(toList()));

    Optional<DiscoveryDocument> bestDoc =
        ioSafeGet(docs.thenApply(d -> {
          Optional<DiscoveryDocument> exactMatch =
              d.stream().filter(x -> x.getUrls().contains(url)).findFirst();

          // TODO find the longest matching prefix
          // exact url match or just first doc
          return Util.or(exactMatch, () -> d.stream().findFirst());
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
      } else {
        outputHandler.info("No specific API found");
        outputHandler.info("https://developers.google.com/apis-explorer/#p/");
      }
  }
}
