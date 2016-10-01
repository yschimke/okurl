package com.baulsupp.oksocial.brave;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.okhttp.OkHttpParser;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import okhttp3.Handshake;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import zipkin.TraceKeys;

public class OkSocialParser extends OkHttpParser {

  public List<KeyValueAnnotation> networkRequestTags(Request request) {
    List<KeyValueAnnotation> tags = Lists.newArrayList();

    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_URL, request.url().toString()));
    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_METHOD, request.method()));
    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_HOST, request.url().host()));
    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_PATH, request.url().encodedPath()));

    //try {
    //  long contentLength = request.body().contentLength();
    //  if (contentLength > -1) {
    //    tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_REQUEST_SIZE, "" + contentLength));
    //  }
    //} catch (IOException e) {
    //  // TODO worth warning?
    //}

    addHeaders(tags, request.headers(), "http.request.header");

    System.out.println(tags);

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

    tags.add(
        KeyValueAnnotation.create(TraceKeys.HTTP_STATUS_CODE, String.valueOf(response.code())));
    tags.add(KeyValueAnnotation.create("http.protocol", response.protocol().toString()));

    //long contentLength = response.body().contentLength();
    //if (contentLength > -1) {
    //  tags.add(KeyValueAnnotation.create(TraceKeys.HTTP_RESPONSE_SIZE, "" + contentLength));
    //}

    Handshake handshake = response.handshake();
    if (handshake != null) {
      tags.add(KeyValueAnnotation.create("http.tls", handshake.tlsVersion().toString()));
      tags.add(KeyValueAnnotation.create("http.cipher", handshake.cipherSuite().toString()));
      tags.add(KeyValueAnnotation.create("http.peer", handshake.peerPrincipal().toString()));
    }

    addHeaders(tags, response.headers(), "http.response.header");

    System.out.println(tags);

    return tags;
  }
}
