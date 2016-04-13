package com.baulsupp.oksocial.twitter;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public class TwitterUtil {
  private TwitterUtil() {
  }

  public static final Set<String> TWITTER_API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "api.twitter.com", "upload.twitter.com", "stream.twitter.com",
          "mobile.twitter.com", "syndication.twitter.com", "pbs.twimg.com",
          "t.co")
      );

  public static final Set<String> TWITTER_WEB_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "www.twitter.com", "twitter.com")
      );

  public static final Set<String> TWITTER_HOSTS =
      Collections.unmodifiableSet(Sets.union(TWITTER_API_HOSTS, TWITTER_WEB_HOSTS));
}
