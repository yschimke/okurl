package com.baulsupp.oksocial.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.X509TrustManager;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

public class MergedX509TrustManager implements X509TrustManager {
  private List<X509TrustManager> managers;

  public MergedX509TrustManager(List<X509TrustManager> managers) {
    this.managers = managers;
  }

  @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    throw new UnsupportedOperationException();
  }

  @Override public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    List<CertificateException> exceptions = newArrayList();

    for (X509TrustManager tm : managers) {
      try {
        tm.checkServerTrusted(chain, authType);
        return;
      } catch (CertificateException e) {
        exceptions.add(e);
      }
    }

    throw bestException(exceptions);
  }

  public CertificateException bestException(List<CertificateException> exceptions)
      throws CertificateException {
    if (exceptions.size() > 0) {
      // last is probably system keystore
      throw exceptions.get(exceptions.size() - 1);
    } else {
      throw new CertificateException("no X509TrustManager to check");
    }
  }

  @Override public X509Certificate[] getAcceptedIssuers() {
    List<X509Certificate> certificates = newArrayList();

    for (X509TrustManager tm : managers) {
      certificates.addAll(asList(tm.getAcceptedIssuers()));
    }

    return certificates.toArray(new X509Certificate[0]);
  }
}
