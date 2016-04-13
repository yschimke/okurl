package com.baulsupp.oksocial.facebook;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public class FacebookUtil {
  private FacebookUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "graph.facebook.com")
      );
}
