package com.baulsupp.oksocial.stackoverflow;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class StackOverflowUtil {
  private StackOverflowUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.stackexchange.com")
      );
}
