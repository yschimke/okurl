package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.apidocs.ApiDocPresenter;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.util.FutureUtil;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;

public class DiscoveryApiDocPresenter implements ApiDocPresenter {
  @Override public void explainApi(String url, OutputHandler outputHandler, OkHttpClient client)
      throws IOException {
    DiscoveryIndex discoveryIndex = DiscoveryIndex.loadStatic();
    List<String> discoveryPaths = discoveryIndex.getDiscoveryUrlForPrefix(url);

    DiscoveryRegistry registry = DiscoveryRegistry.loadStatic();

    // TODO query concurrently find first
    for (String path : discoveryPaths) {
      DiscoveryDocument s = FutureUtil.ioSafeGet(registry.load(client, path));

      if (s.getUrls().contains(url)) {
        outputHandler.info("name: " + s.getApiName());
        outputHandler.info("docs: " + s.getDocLink());
        return;
      }
    }

    // TODO check base url and present if single result

    return;
  }
}
