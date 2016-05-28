package com.baulsupp.oksocial.services.stackexchange;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class StackExchangeUtil {
  private StackExchangeUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.stackexchange.com")
      );
}
