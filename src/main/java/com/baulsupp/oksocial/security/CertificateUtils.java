package com.baulsupp.oksocial.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CertificateUtils {
  public static X509TrustManager load(List<File> serverCerts)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    TrustManagerFactory tmf = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null);

    for (int i = 0; i < serverCerts.size(); i++) {
      try (InputStream is = new FileInputStream(serverCerts.get(i))) {
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);
        ks.setCertificateEntry("cacrt." + i, caCert);
      }
    }

    tmf.init(ks);

    return (X509TrustManager) tmf.getTrustManagers()[0];
  }
}
