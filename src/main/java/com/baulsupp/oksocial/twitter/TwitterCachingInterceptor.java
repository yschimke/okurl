package com.baulsupp.oksocial.twitter;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import okhttp3.Interceptor;
import okhttp3.Response;

public class TwitterCachingInterceptor implements Interceptor {
  private static final Set<String> permanentHosts = Sets.newHashSet("pbs.twimg.com");

  @Override public Response intercept(Chain chain) throws IOException {
    Response originalResponse = chain.proceed(chain.request());

    String host = chain.request().url().host();

    if (TwitterUtil.TWITTER_API_HOSTS.contains(host)) {
      if (originalResponse.code() == 200) {
        int cacheSeconds = 60;

        if (permanentHosts.contains(host)) {
          cacheSeconds = 3600;
        }

        return originalResponse.newBuilder()
            .header("Cache-Control", "max-age=" + cacheSeconds)
            .removeHeader("expires")
            .removeHeader("pragma")
            .build();
      }
    }

    return originalResponse;
  }
}
