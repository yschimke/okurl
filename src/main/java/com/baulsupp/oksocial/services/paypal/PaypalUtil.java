package com.baulsupp.oksocial.services.paypal;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class PaypalUtil {
  private PaypalUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.paypal.com", "api.sandbox.paypal.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.paypal.com" + s).build();
  }
}
