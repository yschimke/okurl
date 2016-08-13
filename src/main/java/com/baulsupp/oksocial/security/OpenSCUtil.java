package com.baulsupp.oksocial.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class OpenSCUtil {
  public static KeyManager[] getKeyManagers(char[] password, int slot) throws Exception {
    String config =
        "name=OpenSC\nlibrary=/Library/OpenSC/lib/opensc-pkcs11.so\nslot=" + slot + "\n";

    Provider pkcs11;

    KeyStore keystore;
    if (isJava9()) {
      pkcs11 = Security.getProvider("SunPKCS11");

      pkcs11 =
          (Provider) Provider.class.getMethod("configure", String.class).invoke(pkcs11, "--" + config);

      Security.addProvider(pkcs11);

      keystore = KeyStore.getInstance("PKCS11", pkcs11);
    } else {
      Constructor<?> ctor =
          Class.forName("sun.security.pkcs11.SunPKCS11").getConstructor(InputStream.class);
      pkcs11 =
          (Provider) ctor.newInstance(
              new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));

      Security.addProvider(pkcs11);

      keystore = KeyStore.getInstance("PKCS11", pkcs11);
    }

    CallbackHandler handler = new CallbackHandler() {
      @Override public void handle(Callback[] callbacks)
          throws IOException, UnsupportedCallbackException {
        System.out.println(Arrays.toString(callbacks));
        System.exit(0);
      }
    };
    KeyStore.CallbackHandlerProtection ch = new KeyStore.CallbackHandlerProtection(handler);
    keystore.load(() -> ch);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
    kmf.init(keystore, null);

    return kmf.getKeyManagers();
  }

  private static boolean isJava9() {
    return "9".equals(System.getProperty("java.specification.version"));
  }
}
