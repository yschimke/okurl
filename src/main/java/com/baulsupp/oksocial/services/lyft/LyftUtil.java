package com.baulsupp.oksocial.services.lyft;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class LyftUtil {
  public static final Collection<String> SCOPES = Arrays.asList("public",
      "rides.read",
      "offline",
      "rides.request",
      "profile");

  private LyftUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.lyft.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.lyft.com" + s).build();
  }
}
