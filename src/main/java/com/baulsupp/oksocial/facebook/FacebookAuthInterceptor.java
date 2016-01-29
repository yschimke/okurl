package com.baulsupp.oksocial.facebook;

import okhttp3.*;

import java.io.IOException;
import java.util.Iterator;

public class FacebookAuthInterceptor implements Interceptor {
  private FacebookCredentials facebookCredentials;

  public FacebookAuthInterceptor(FacebookCredentials facebookCredentials) {
    this.facebookCredentials = facebookCredentials;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    if (requiresServerAuth(request)) {
      String token = facebookCredentials.accessToken;
      token = "CAAH0ZBiEkcg8BAPp8tVQsegHnJUgw6gifx63JfF4LRwZA5xZCl58HAzpYiZADDKIkU0xA4sIKjGO4pYwLOYm3QarQ2r0nvfQb4qp8FHSoFl8zzLTv1xuUDxup2xe3SSgN5JtAofMjL27P1ZC1s2sg1izAczUcvaINVlIa5YHje58sByuViQAK1jle573k8uKNw3OVMUEtpfe4aZAB2ZA5s5iZBkU0iJkn7UZD";

      HttpUrl newUrl = request.url().newBuilder().addQueryParameter("access_token", token).build();

      request =
          request.newBuilder().url(newUrl).build();
    }

    return chain.proceed(request);
  }

  public boolean requiresServerAuth(Request request) {
    String host = request.url().host();

    return FacebookUtil.API_HOSTS.contains(host);
  }

  public static void remove(OkHttpClient.Builder builder) {
    Iterator<Interceptor> i = builder.networkInterceptors().iterator();
    while (i.hasNext()) {
      Interceptor interceptor = i.next();

      if (interceptor instanceof FacebookAuthInterceptor) {
        i.remove();
      }
    }
  }

  public static OkHttpClient updateCredentials(OkHttpClient client,
      FacebookCredentials newCredentials) {
    OkHttpClient.Builder builder = client.newBuilder();

    remove(builder);
    builder.networkInterceptors().add(new FacebookAuthInterceptor(newCredentials));
    return builder.build();
  }
}
