package com.baulsupp.oksocial.util;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class WrappedRequestBody extends RequestBody {
  private final String contentType;
  private final RequestBody body;

  public WrappedRequestBody(RequestBody body, String contentType) {
    this.body = body;
    this.contentType = contentType;
  }

  @Override public MediaType contentType() {
    return MediaType.parse(contentType);
  }

  @Override public void writeTo(BufferedSink bufferedSink) throws IOException {
    body.writeTo(bufferedSink);
  }
}
