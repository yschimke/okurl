package com.baulsupp.okurl.security

import org.conscrypt.Conscrypt
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object CertificateUtils {

  fun load(serverCerts: List<File>): X509TrustManager {
    return trustManagerForKeyStore(keyStoreForCerts(serverCerts))
  }

  fun trustManagerForKeyStore(ks: KeyStore?): X509TrustManager {
    val tmf = TrustManagerFactory.getInstance(
      TrustManagerFactory.getDefaultAlgorithm(),
      Conscrypt.newProviderBuilder().provideTrustManager(true).build()
    )

    tmf.init(ks)

    return tmf.trustManagers[0] as X509TrustManager
  }

  fun keyStoreForCerts(serverCerts: List<File>): KeyStore {
    val cf = CertificateFactory.getInstance("X.509")

    val ks = KeyStore.getInstance(KeyStore.getDefaultType())
    ks.load(null)

    for (i in serverCerts.indices) {
      FileInputStream(serverCerts[i]).use { `is` ->
        val caCert = cf.generateCertificate(`is`) as X509Certificate
        ks.setCertificateEntry("cacrt.$i", caCert)
      }
    }
    return ks
  }

  fun combineTrustManagers(trustManagers: MutableList<X509TrustManager>, includedDir: File? = null): X509TrustManager {
    val localCerts = includedCertificates(includedDir)
    if (localCerts != null) {
      trustManagers.add(load(localCerts))
    }
    trustManagers.add(systemTrustManager())

    if (trustManagers.size == 1) {
      return trustManagers.first()
    }

    return MergedX509TrustManager(trustManagers)
  }

  fun systemTrustManager() = trustManagerForKeyStore(null)

  fun includedCertificates(includedDir: File?): List<File>? {
    if (includedDir == null)
      return listOf()

    return includedDir.listFiles { f -> f.name.endsWith(".crt") }?.toList()
  }
}
