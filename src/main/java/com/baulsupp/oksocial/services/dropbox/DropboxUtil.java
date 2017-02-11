package com.baulsupp.oksocial.services.dropbox;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import okhttp3.Request;

public class DropboxUtil {
  private DropboxUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet("api.dropboxapi.com", "content.dropboxapi.com"));

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.dropboxapi.com" + s).build();
  }
}
