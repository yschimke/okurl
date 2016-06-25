package com.baulsupp.oksocial.services.twilio;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class TwilioUtil {
  private TwilioUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.twilio.com")
      );
}
