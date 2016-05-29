package com.baulsupp.oksocial.services.stackexchange;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class StackExchangeUtil {
  public static final Collection<String> SCOPES = Arrays.asList("read_inbox",
      "no_expiry",
      "write_access",
      "private_info");

  private StackExchangeUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.stackexchange.com")
      );
}
