package com.baulsupp.oksocial.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.junit.Test;

public class KeystoreUtilsTest {
  @Test public void loadEmptyPassword()
      throws Exception {
    File f = writeFile("");

    KeystoreUtils.createLocalKeyManager(f, passwordCallback(""));
  }

  @Test public void loadNonEmptyPassword()
      throws Exception {
    File f = writeFile("a");

    KeystoreUtils.createLocalKeyManager(f, passwordCallback("a"));
  }

  private KeyStore.CallbackHandlerProtection passwordCallback(String mypass) {
    return new KeyStore.CallbackHandlerProtection(callbacks -> {
      for (Callback c : callbacks) {
        if (c instanceof PasswordCallback) {
          PasswordCallback pw = (PasswordCallback) c;
          System.out.println("PROMPT> " + pw.getPrompt());
          pw.setPassword(mypass.toCharArray());
        } else {
          throw new UnsupportedCallbackException(c);
        }
      }
    });
  }

  public static File writeFile(String keyPw)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      InvalidKeyException, NoSuchProviderException, SignatureException {
    File temp = File.createTempFile("tempkey", ".keystore");
    temp.deleteOnExit();

    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(512);
    KeyPair keyPair = keyGen.genKeyPair();
    PrivateKey privateKey = keyPair.getPrivate();

    keyStore.setKeyEntry("a", privateKey, keyPw.toCharArray(),
        generateCertificate());

    try (FileOutputStream fos = new FileOutputStream(temp)) {
      keyStore.store(fos, "123456".toCharArray());
    }

    return temp;
  }

  private static String certificate = "-----BEGIN CERTIFICATE-----\n"
      + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
      + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
      + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
      + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
      + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
      + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
      + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
      + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
      + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
      + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
      + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
      + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
      + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
      + "-----END CERTIFICATE-----\n";

  public static Certificate[] generateCertificate()
      throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException,
      InvalidKeyException, SignatureException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return new Certificate[] {
        cf.generateCertificate(new ByteArrayInputStream(certificate.getBytes()))};
  }
}