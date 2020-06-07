package com.baulsupp.okurl.security

import java.security.KeyStore
import java.security.Security
import java.util.Arrays
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.KeyStoreBuilderParameters
import javax.net.ssl.X509ExtendedKeyManager
import javax.security.auth.callback.CallbackHandler

object OpenSCUtil {
  fun getKeyManager(
    callbackHandler: CallbackHandler,
    slot: Int = 0
  ): X509ExtendedKeyManager {
    val config = "--name=OpenSC\nlibrary=/Library/OpenSC/lib/opensc-pkcs11.so\nslot=$slot\n"

    // May fail with ProviderException with root cause like
    // sun.security.pkcs11.wrapper.PKCS11Exception: CKR_SLOT_ID_INVALID
    val pkcs11 = Security.getProvider("SunPKCS11").configure(config)
    Security.addProvider(pkcs11)

    val builderList: List<KeyStore.Builder> = Arrays.asList(
      KeyStore.Builder.newInstance("PKCS11", null, KeyStore.CallbackHandlerProtection(callbackHandler))
    )

    val keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509")
    keyManagerFactory.init(KeyStoreBuilderParameters(builderList))
    return keyManagerFactory.keyManagers[0] as X509ExtendedKeyManager
  }
}
