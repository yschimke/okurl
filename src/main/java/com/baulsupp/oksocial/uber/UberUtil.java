package com.baulsupp.oksocial.uber;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class UberUtil {
  private UberUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.uber.com")
      );
}
