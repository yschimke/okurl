package com.baulsupp.oksocial.services.fitbit;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class FitbitUtil {
  public static final Collection<String> SCOPES =
      Arrays.asList("activity", "heartrate", "location", "nutrition", "profile",
          "settings", "sleep", "social", "weight");

  private FitbitUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.fitbit.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.fitbit.com" + s).build();
  }
}
