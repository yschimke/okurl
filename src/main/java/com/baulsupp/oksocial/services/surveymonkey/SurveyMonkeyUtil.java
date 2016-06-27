package com.baulsupp.oksocial.services.surveymonkey;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class SurveyMonkeyUtil {
  public static final Collection<String> SCOPES = Arrays.asList();

  private SurveyMonkeyUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.surveymonkey.net")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.surveymonkey.net" + s).build();
  }
}
