package com.baulsupp.oksocial.uber;

import java.io.IOException;
import java.util.Iterator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UberAuthInterceptor implements Interceptor {
  private UberServerCredentials serverCredentials;

  public UberAuthInterceptor(UberServerCredentials serverCredentials) {
    this.serverCredentials = serverCredentials;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    if (requiresServerAuth(request)) {
      String token = serverCredentials.serverToken;

      request =
          request.newBuilder().addHeader("Authorization", "Token " + token).build();
    }

    return chain.proceed(request);
  }

  public boolean requiresServerAuth(Request request) {
    String host = request.url().host();

    return UberUtil.API_HOSTS.contains(host);
  }

  public static void remove(OkHttpClient.Builder builder) {
    Iterator<Interceptor> i = builder.networkInterceptors().iterator();
    while (i.hasNext()) {
      Interceptor interceptor = i.next();

      if (interceptor instanceof UberAuthInterceptor) {
        i.remove();
      }
    }
  }

  public static OkHttpClient updateCredentials(OkHttpClient client,
      UberServerCredentials newCredentials) {
    OkHttpClient.Builder builder = client.newBuilder();

    remove(builder);
    builder.networkInterceptors().add(new UberAuthInterceptor(newCredentials));
    return builder.build();
  }
}
