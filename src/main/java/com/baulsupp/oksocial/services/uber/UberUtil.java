package com.baulsupp.oksocial.services.uber;

import okhttp3.Request;

public class UberUtil {
  private UberUtil() {
  }

  public static Request apiRequest(String s, Request.Builder requestBuilder) {
    return requestBuilder.url("https://api.uber.com" + s).build();
  }
}
