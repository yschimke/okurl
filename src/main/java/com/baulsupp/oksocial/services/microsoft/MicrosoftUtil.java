package com.baulsupp.oksocial.services.microsoft;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class MicrosoftUtil {
  private MicrosoftUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "graph.microsoft.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://graph.microsoft.com" + s).build();
  }
}
