package com.baulsupp.oksocial.services.google;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class GoogleUtil {
  public static final Collection<String> SCOPES =
      Arrays.asList("plus.login", "plus.profile.emails.read");

  private GoogleUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.google.com")
      );

  public static String fullScope(String suffix) {
    return suffix.contains("/") ? suffix : "https://www.googleapis.com/auth/" + suffix;
  }
}
