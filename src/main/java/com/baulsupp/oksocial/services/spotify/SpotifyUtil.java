package com.baulsupp.oksocial.services.spotify;

import java.util.Arrays;
import java.util.Collection;
import okhttp3.Request;

public class SpotifyUtil {
  public static final Collection<String> SCOPES = Arrays.asList("playlist-read-private",
      "playlist-read-collaborative",
      "playlist-modify-public",
      "playlist-modify-private",
      "streaming",
      "ugc-image-upload",
      "user-follow-modify",
      "user-follow-read",
      "user-library-read",
      "user-library-modify",
      "user-read-private",
      "user-read-birthdate",
      "user-read-email",
      "user-top-read");

  private SpotifyUtil() {
  }

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.spotify.com" + s).build();
  }


}
