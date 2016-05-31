package com.baulsupp.oksocial.output;

import okhttp3.MediaType;
import okio.Okio;
import okio.Sink;

public class OutputUtil {
  public static String getExtension(MediaType mediaType) {
    if (mediaType != null) {
      String simpleType = mediaType.type() + "/" + mediaType.subtype();

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

  public static Sink systemOut() {
    return Okio.sink(System.out);
  }
}
