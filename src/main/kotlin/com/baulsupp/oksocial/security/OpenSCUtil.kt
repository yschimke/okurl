package com.baulsupp.oksocial.security

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.Provider
import java.security.Security
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory

object OpenSCUtil {
  @Throws(Exception::class)
  fun getKeyManagers(password: ConsoleCallbackHandler, slot: Int): Array<KeyManager> {
    val config = "name=OpenSC\nlibrary=/Library/OpenSC/lib/opensc-pkcs11.so\nslot=$slot\n"

    var pkcs11: Provider

    val keystore: KeyStore
    if (isJava9) {
      pkcs11 = Security.getProvider("SunPKCS11")

      pkcs11 = Provider::class.java.getMethod("configure", String::class.java)
        .invoke(pkcs11, "--" + config) as Provider

      Security.addProvider(pkcs11)

      keystore = KeyStore.getInstance("PKCS11", pkcs11)
    } else {
      val ctor = Class.forName("sun.security.pkcs11.SunPKCS11").getConstructor(InputStream::class.java)
      pkcs11 = ctor.newInstance(
        ByteArrayInputStream(config.toByteArray(StandardCharsets.UTF_8))) as Provider

      Security.addProvider(pkcs11)

      keystore = KeyStore.getInstance("PKCS11", pkcs11)
    }

    keystore.load { KeyStore.CallbackHandlerProtection(password) }

    val kmf = KeyManagerFactory.getInstance("NewSunX509")
    kmf.init(keystore, null)

    return kmf.keyManagers
  }

  private val isJava9: Boolean
    get() = "9" == System.getProperty("java.specification.version")
}
