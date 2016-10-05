package com.baulsupp.oksocial.services.mapbox;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class MapboxUtil {
  private MapboxUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.mapbox.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.mapbox.com" + s).build();
  }
}
