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
    return trustManagerForKeyStore(keyStoreForCerts(serverCerts));
  }

  public static X509TrustManager trustManagerForKeyStore(KeyStore ks)
      throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

    tmf.init(ks);

    return (X509TrustManager) tmf.getTrustManagers()[0];
  }

  public static KeyStore keyStoreForCerts(List<File> serverCerts)
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null);

    for (int i = 0; i < serverCerts.size(); i++) {
      try (InputStream is = new FileInputStream(serverCerts.get(i))) {
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);
        ks.setCertificateEntry("cacrt." + i, caCert);
      }
    }
    return ks;
  }

  public static X509TrustManager combineTrustManagers(List<X509TrustManager> trustManagers)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
    trustManagers.add(load(includedCertificates()));
    trustManagers.add(systemTrustManager());

    return new CompositeX509TrustManager(trustManagers);
  }

  public static X509TrustManager systemTrustManager()
      throws NoSuchAlgorithmException, KeyStoreException {
    return trustManagerForKeyStore(null);
  }

  public static List<File> includedCertificates() {
    String installDir = System.getenv("INSTALLDIR");
    if (installDir == null) {
      installDir = ".";
    }

    File[] files =
        new File(installDir, "certificates").listFiles(f -> f.getName().endsWith(".crt"));

    if (files != null) {
      return Arrays.asList(files);
    }

    return null;
  }
}
