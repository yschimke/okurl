package com.baulsupp.oksocial.services;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.ServiceInterceptor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class UrlCompleter {
  private final ServiceInterceptor serviceInterceptor;

  public UrlCompleter(ServiceInterceptor serviceInterceptor) {
    this.serviceInterceptor = serviceInterceptor;
  }

  public List<String> urlList(String s) {
    List<String> list = new ArrayList<>(100);

    for (AuthInterceptor<?> a : serviceInterceptor.services()) {
      list.addAll(a.completions(s, false));
    }

    list.sort(Comparator.naturalOrder());

    return list.stream().map(h -> "https://" + h + "/").collect(toList());
  }
}
