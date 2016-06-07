package com.baulsupp.oksocial.services.google;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class GoogleUtil {
  public static final Collection<String> SCOPES = Arrays.asList("profile", "email");

  private GoogleUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.google.com", "googleapis.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.google.com" + s).build();
  }
}
