package com.baulsupp.oksocial.services.github;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class GithubUtil {
  public static final Collection<String> SCOPES = Arrays.asList(
      "user",
      "repo",
      "gist",
      "admin:org");

  private GithubUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.github.com", "uploads.github.com")
      );

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.github.com" + s).build();
  }
}
