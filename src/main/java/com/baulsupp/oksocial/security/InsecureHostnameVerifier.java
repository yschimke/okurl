package com.baulsupp.oksocial.security;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class InsecureHostnameVerifier implements HostnameVerifier {
  @Override public boolean verify(String s, SSLSession sslSession) {
    return true;
  }
}
