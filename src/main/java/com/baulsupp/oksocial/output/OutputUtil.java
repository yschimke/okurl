package com.baulsupp.oksocial.output;

import okhttp3.MediaType;
import okio.Okio;
import okio.Sink;

public class OutputUtil {
  public static boolean isMedia(MediaType mediaType) {
    return "image".equals(mediaType.type()) || "pdf".equals(mediaType.subtype());
  }

  public static String getExtension(MediaType mediaType) {
    if (mediaType != null) {
      String simpleType = mediaTypeString(mediaType);

      switch (simpleType) {
        case "image/jpeg":
          return ".jpg";
        case "image/gif":
          return ".gif";
        case "image/png":
          return ".png";
        default:
          return ".data";
      }
    }

    return ".data";
  }

  private static String mediaTypeString(MediaType mediaType) {
    return mediaType.type() + "/" + mediaType.subtype();
  }

  public static Sink systemOut() {
    return Okio.sink(System.out);
  }

  public static boolean isJson(MediaType mediaType) {
    return isMediaType(mediaType, "application/json", "text/json");
  }

  public static boolean isMediaType(MediaType mediaType, String... types) {
    String mts = mediaTypeString(mediaType);

    for (String type: types) {
      if (mts.equals(type)) {
        return true;
      }
    }

    return false;
  }
}
