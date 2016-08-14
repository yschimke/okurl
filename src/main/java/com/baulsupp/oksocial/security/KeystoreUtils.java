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
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class KeystoreUtils {
  public static KeyManager[] keyManagerArray(List<KeyManager> keyManagers) {
    KeyManager[] kms = null;
    if (!keyManagers.isEmpty()) {
      kms = keyManagers.toArray(new KeyManager[0]);
    }
    return kms;
  }

  public static SSLSocketFactory createSslSocketFactory(KeyManager[] keyManagers,
      X509TrustManager trustManagers) throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext context = SSLContext.getInstance("TLS");

    context.init(keyManagers, new TrustManager[] {trustManagers}, null);

    return context.getSocketFactory();
  }

  public static KeyManager createKeyManager(KeyStore keystore, CallbackHandler callbackHandler)
      throws Exception {
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

    try {
      kmf.init(keystore, "".toCharArray());
    } catch (UnrecoverableKeyException uke) {
      PasswordCallback pwCallback = new PasswordCallback("Keystore password: ", false);
      Callback[] callbacks = new Callback[] {pwCallback};
      callbackHandler.handle(callbacks);

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

    keystore_client.load(keystore != null ? new FileInputStream(keystore) : null, null);

    return keystore_client;
  }
}
