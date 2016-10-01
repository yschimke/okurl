package com.baulsupp.oksocial.services.giphy;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class GiphyUtil {
  private GiphyUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.giphy.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.giphy.com" + s).build();
  }
}
