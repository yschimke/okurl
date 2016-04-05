package com.baulsupp.oksocial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import sun.security.pkcs11.SunPKCS11;

public class OpenSCUtil {
  public static KeyManager[] getKeyManagers(char[] password)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      UnrecoverableKeyException {
    String config = "name=OpenSC\nlibrary=/Library/OpenSC/lib/opensc-pkcs11.so\n";
    SunPKCS11 pkcs11 =
        new SunPKCS11(new ByteArrayInputStream(config.getBytes(Charset.forName("UTF-8"))));
    Security.addProvider(pkcs11);

    KeyStore keystore = KeyStore.getInstance("PKCS11", pkcs11);
    keystore.load(null, password);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
    kmf.init(keystore, null);

    return kmf.getKeyManagers();
  }
}
