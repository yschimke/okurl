package com.baulsupp.oksocial;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

public class OpenSCUtil {
  public static KeyManager[] getKeyManagers(char[] password) throws Exception {
    String config = "name=OpenSC\nlibrary=/Library/OpenSC/lib/opensc-pkcs11.so\n";
    Constructor<?> ctor =
        Class.forName("sun.security.pkcs11.SunPKCS11").getConstructor(InputStream.class);
    Provider pkcs11 =
        (Provider) ctor.newInstance(
            new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
    Security.addProvider(pkcs11);

    KeyStore keystore = KeyStore.getInstance("PKCS11", pkcs11);
    keystore.load(null, password);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
    kmf.init(keystore, null);

    return kmf.getKeyManagers();
  }
}
