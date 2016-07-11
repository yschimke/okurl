package com.baulsupp.oksocial.completion;

import java.io.IOException;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;

public interface ApiCompleter {
  Future<UrlList> prefixUrls() throws IOException;

  Future<UrlList> siteUrls(HttpUrl url) throws IOException;
}
