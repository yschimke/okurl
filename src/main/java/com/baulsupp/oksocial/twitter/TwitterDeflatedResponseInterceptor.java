package com.baulsupp.oksocial.twitter;

import java.io.IOException;
import java.util.zip.Inflater;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.InflaterSource;
import okio.Okio;

public class TwitterDeflatedResponseInterceptor implements Interceptor {
  @Override public Response intercept(Chain chain) throws IOException {
    Response response = chain.proceed(chain.request());

    if ("deflate".equals(response.header("content-encoding"))) {
      String host = response.request().url().host();

      if (TwitterUtil.TWITTER_HOSTS.contains(host)) {
        return response.newBuilder()
            .body(inflateBody(response.body()))
            .removeHeader("content-encoding")
            .removeHeader("content-length")
            .build();
      }
    }

    return response;
  }

  private ResponseBody inflateBody(ResponseBody origBody) throws IOException {
    Inflater inflater = new Inflater();
    BufferedSource realSource = origBody.source();
    InflaterSource s = new InflaterSource(realSource, inflater);

    return ResponseBody.create(origBody.contentType(), -1, Okio.buffer(s));
  }
}
