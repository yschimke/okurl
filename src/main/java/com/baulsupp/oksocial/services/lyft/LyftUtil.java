package com.baulsupp.oksocial.services.lyft;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class LyftUtil {
  private LyftUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.lyft.com")
      );
}
