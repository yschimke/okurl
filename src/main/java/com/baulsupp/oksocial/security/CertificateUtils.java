package com.baulsupp.oksocial.security;

import com.secdec.codedx.security.CompositeX509TrustManager;
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
import java.util.Collections;
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

  public static X509TrustManager loadCombined(List<File> serverCerts)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init((KeyStore) null);
    X509TrustManager systemTrustManager =
        (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

    serverCerts.addAll(includedCertificates());

    if (serverCerts.isEmpty()) {
      return systemTrustManager;
    } else {
      return new CompositeX509TrustManager(load(serverCerts), systemTrustManager);
    }
  }

  public static List<File> includedCertificates() {
    String installDir = System.getenv("INSTALLDIR");

    if (installDir != null) {
      File[] files =
          new File(installDir, "certificates").listFiles(f -> f.getName().endsWith(".crt"));

      if (files != null) {
        return Arrays.asList(files);
      }
    }

    return Collections.emptyList();
  }
}
