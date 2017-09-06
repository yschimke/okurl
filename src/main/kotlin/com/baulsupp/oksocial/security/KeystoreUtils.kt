package com.baulsupp.oksocial.security

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.util.Arrays
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.PasswordCallback
import javax.security.auth.callback.UnsupportedCallbackException

object KeystoreUtils {
    fun keyManagerArray(keyManagers: List<KeyManager>): Array<KeyManager>? {
        var kms: Array<KeyManager>? = null
        if (!keyManagers.isEmpty()) {
            kms = keyManagers.toTypedArray()
        }
        return kms
    }

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    fun createSslSocketFactory(keyManagers: Array<KeyManager>,
                               trustManagers: X509TrustManager): SSLSocketFactory {
        val context = SSLContext.getInstance("TLS")

        context.init(keyManagers, arrayOf<TrustManager>(trustManagers), null)

        return context.socketFactory
    }

    @Throws(Exception::class)
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

    @Throws(KeyStoreException::class, IOException::class, NoSuchAlgorithmException::class, CertificateException::class, UnsupportedCallbackException::class)
    fun getKeyStore(keystore: File?): KeyStore {
        val keystore_client = KeyStore.getInstance("JKS")

        keystore_client.load(if (keystore != null) FileInputStream(keystore) else null, null)

        return keystore_client
    }
}
