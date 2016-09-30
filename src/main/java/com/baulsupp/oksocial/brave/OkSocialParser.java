package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.okhttp.OkHttpParser;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import zipkin.TraceKeys;

public class OkSocialParser extends OkHttpParser {


  public List<KeyValueAnnotation> networkRequestTags(Request request) {
    List<KeyValueAnnotation> tags = Lists.newArrayList();

    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_URL, request.url().toString()));

    addHeaders(tags, request.headers(), "http.request.header");

    return tags;
  }

  private void addHeaders(List<KeyValueAnnotation> tags, Headers headers, String prefix) {
    headers.names().forEach(name -> {
      headers.values(name).forEach(value -> {
        tags.add(KeyValueAnnotation.create(prefix + ":" + name, value));
      });
    });
  }

  public List<KeyValueAnnotation> networkResponseTags(Response response) {
    List<KeyValueAnnotation> tags = Lists.newArrayList();

    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_STATUS_CODE, String.valueOf(response.code())));

    addHeaders(tags, response.headers(), "http.response.header");

    return tags;
  }
}
