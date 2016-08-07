package com.baulsupp.oksocial.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

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

    keystore.load(null, password);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
    kmf.init(keystore, null);

    return kmf.getKeyManagers();
  }

  private static boolean isJava9() {
    return "9".equals(System.getProperty("java.specification.version"));
  }
}
