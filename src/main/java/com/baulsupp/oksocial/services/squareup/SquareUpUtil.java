package com.baulsupp.oksocial.services.squareup;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class SquareUpUtil {
  private SquareUpUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "connect.squareup.com")
      );
}
