package com.baulsupp.oksocial.authenticator;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.Response;
import sun.misc.Service;

public class ServiceInterceptor implements Interceptor {
  private List<AuthInterceptor> services;

  public ServiceInterceptor() {
    services = Lists.newArrayList(Service.providers(AuthInterceptor.class));
  }

  @Override public Response intercept(Chain chain) throws IOException {
    for (AuthInterceptor interceptor : services) {
      if (interceptor.supportsUrl(chain.request().url())) {
        return interceptor.intercept(chain);
      }
    }

    return chain.proceed(chain.request());
  }

  public Iterable<AuthInterceptor> services() {
    return services;
  }
}
