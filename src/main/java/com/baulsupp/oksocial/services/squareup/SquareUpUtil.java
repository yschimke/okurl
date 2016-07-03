package com.baulsupp.oksocial.services.squareup;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class SquareUpUtil {
  private SquareUpUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "connect.squareup.com")
      );

  public static Collection<String> ALL_PERMISSIONS = Arrays.asList(
      "MERCHANT_PROFILE_READ",
      "PAYMENTS_READ",
      "SETTLEMENTS_READ",
      "BANK_ACCOUNTS_READ"
  );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://connect.squareup.com" + s).build();
  }
}
