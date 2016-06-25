package com.baulsupp.oksocial.services.twilio;

import com.baulsupp.oksocial.authenticator.BasicCredentials;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class TwilioUtil {
  private TwilioUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.twilio.com")
      );

  public static Request apiRequest(String path,
      Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.twilio.com" + path).build();
  }

  public static Request accountRequest(BasicCredentials credentials, String path,
      Request.Builder requestBuilder) {
    return requestBuilder.url(
        "https://api.twilio.com/2010-04-01/Accounts/" + credentials.user + path).build();
  }
}
