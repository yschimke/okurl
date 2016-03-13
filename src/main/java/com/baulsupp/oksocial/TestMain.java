package com.baulsupp.oksocial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

public class TestMain {
  public static void main(String[] args)
      throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
      KeyStoreException, IOException {
    System.setProperty("javax.net.debug", "all");

    X509ExtendedKeyManager km = (X509ExtendedKeyManager) getKeyManagers("3392".toCharArray(), 0)[0];

    Socket s = new Socket("localhost", 44330);

    String alias = km.chooseClientAlias(new String[] {"RSA", "DSA"}, null, s);

    System.out.println(alias);
    System.out.println(Arrays.toString(km.getClientAliases("RSA", null)));
    System.out.println(Arrays.toString(km.getClientAliases("RSA", null)));

    alias = km.chooseClientAlias(new String[] {"RSA"}, null, s);

    System.out.println(alias);

    PrivateKey key = km.getPrivateKey(alias);
    System.out.println(key);

    key = km.getPrivateKey("Authentication");
    System.out.println(key);
  }

  public static KeyManager[] getKeyManagers(char[] password, int slot)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
      UnrecoverableKeyException {
    Security.removeProvider("IAIK");

    Provider provider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
    Security.addProvider(provider);

    String config =
        "name=OpenSC\nlibrary=/Applications/qdigidocclient.app/Contents/MacOS/esteid-pkcs11.so\nslotListIndex="
            + slot;

    sun.security.pkcs11.SunPKCS11 pkcs11 =
        new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(config.getBytes()));

    Security.addProvider(pkcs11);

    //debugProviders();

    KeyStore keystore = KeyStore.getInstance("PKCS11", pkcs11);

    keystore.load(null, password);

    //debugKeys(keystore);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
    kmf.init(keystore, null);

    return kmf.getKeyManagers();
  }

  public static void debugKeys(KeyStore keystore) throws KeyStoreException {
    Enumeration<String> aliases = keystore.aliases();

    while (aliases.hasMoreElements()) {
      String s = aliases.nextElement();

      Certificate k = keystore.getCertificate(s);

      System.out.println(k);
    }
  }

  public static void debugProviders() {
    Provider[] providers = Security.getProviders();
    for (Provider p : providers) {
      System.out.println("\n\n" + p.getName());
      Set<Provider.Service> services = p.getServices();

      for (Provider.Service s : services) {
        System.out.println(s.getType() + " " + s.getAlgorithm());
      }
    }
  }
}
