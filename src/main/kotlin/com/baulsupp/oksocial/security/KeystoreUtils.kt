package com.baulsupp.oksocial.security

import okhttp3.internal.platform.Platform
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import java.util.Arrays
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.PasswordCallback

object KeystoreUtils {
  fun keyManagerArray(keyManagers: List<KeyManager>): Array<KeyManager>? {
    var kms: Array<KeyManager>? = null
    if (!keyManagers.isEmpty()) {
      kms = keyManagers.toTypedArray()
    }
    return kms
  }

  fun createSslSocketFactory(keyManagers: Array<KeyManager>?,
                             trustManagers: X509TrustManager): SSLSocketFactory {
    val context = Platform.get().sslContext

    context.init(keyManagers, arrayOf<TrustManager>(trustManagers), null)

    return context.socketFactory
  }

  fun createKeyManager(keystore: KeyStore, callbackHandler: CallbackHandler): KeyManager {
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())

    try {
      kmf.init(keystore, "".toCharArray())
    } catch (uke: UnrecoverableKeyException) {
      val pwCallback = PasswordCallback("Keystore password: ", false)
      val callbacks = arrayOf<Callback>(pwCallback)
      callbackHandler.handle(callbacks)

      kmf.init(keystore, pwCallback.password)
    }

    val keyManagers = kmf.keyManagers

    if (keyManagers.size != 1) {
      throw IllegalStateException(Arrays.toString(keyManagers))
    }

    return keyManagers[0]
  }

  fun getKeyStore(keystore: File?): KeyStore {
    val keystoreClient = KeyStore.getInstance("JKS")

    keystoreClient.load(if (keystore != null) FileInputStream(keystore) else null, null)

    return keystoreClient
  }
}
