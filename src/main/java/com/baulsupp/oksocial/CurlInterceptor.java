package com.baulsupp.oksocial;

import java.io.IOException;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class CurlInterceptor implements okhttp3.Interceptor {
  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    StringBuffer buffy = new StringBuffer(1024);

    buffy.append("curl ");

    Headers headers = request.headers();

    for (String name : headers.names()) {
      for (String value : headers.values(name)) {
        buffy.append("-H \"");
        buffy.append(name);
        buffy.append(": ");
        buffy.append(value.replace("\"", "\\\""));
        buffy.append("\" ");
      }
    }

    buffy.append(request.url());

    System.err.println(buffy);

    return chain.proceed(request);
  }
}
