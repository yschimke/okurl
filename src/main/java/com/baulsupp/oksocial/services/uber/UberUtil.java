package com.baulsupp.oksocial.services.uber;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class UberUtil {
  private UberUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.uber.com", "sandbox-api.uber.com", "login.uber.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.uber.com" + s).build();
  }
}
