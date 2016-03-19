package com.baulsupp.oksocial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

public class OpenSCUtil {
  public static KeyManager[] getKeyManagers(char[] password)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      UnrecoverableKeyException {
    String config =
        "name=OpenSC\n" +
            "library=/Library/OpenSC/lib/opensc-pkcs11.so\n";

    sun.security.pkcs11.SunPKCS11 pkcs11 =
        new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(config.getBytes()));

    Security.addProvider(pkcs11);

    KeyStore keystore = KeyStore.getInstance("PKCS11", pkcs11);

    keystore.load(null, password);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
    kmf.init(keystore, null);

    return kmf.getKeyManagers();
  }
}
