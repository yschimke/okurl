package com.baulsupp.oksocial.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class KeystoreUtils {
  public KeyStore.CallbackHandlerProtection passwordCallback(String mypass) {
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

  public static SSLSocketFactory createSslSocketFactory(KeyManager[] keyManagers,
      X509TrustManager trustManagers) throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext context = SSLContext.getInstance("TLS");

    context.init(keyManagers, new TrustManager[] {trustManagers}, null);

    return context.getSocketFactory();
  }

  public static KeyManager createLocalKeyManager(File keystoreFile,
      KeyStore.CallbackHandlerProtection protection)
      throws Exception {
    KeyStore keystore = getKeyStore(keystoreFile);
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

    try {
      kmf.init(keystore, "".toCharArray());
    } catch (UnrecoverableKeyException uke) {
      PasswordCallback pwCallback = new PasswordCallback("Keystore password: ", false);
      Callback[] callbacks = new Callback[] {pwCallback};
      protection.getCallbackHandler().handle(callbacks);

      kmf.init(keystore, pwCallback.getPassword());
    }

    KeyManager[] keyManagers = kmf.getKeyManagers();

    if (keyManagers.length != 1) {
      throw new IllegalStateException(Arrays.toString(keyManagers));
    }

    return keyManagers[0];
  }

  public static KeyStore getKeyStore(File keystore)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
      UnsupportedCallbackException {
    KeyStore keystore_client = KeyStore.getInstance("JKS");

    keystore_client.load(new FileInputStream(keystore), null);

    return keystore_client;
  }
}
