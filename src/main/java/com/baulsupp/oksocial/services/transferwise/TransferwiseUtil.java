package com.baulsupp.oksocial.services.transferwise;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class TransferwiseUtil {
  private TransferwiseUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.transferwise.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.transferwise.com" + s).build();
  }
}
