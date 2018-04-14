package com.baulsupp.oksocial.security

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
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())

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

  fun combineTrustManagers(trustManagers: MutableList<X509TrustManager>): X509TrustManager {
    val localCerts = includedCertificates()
    if (localCerts != null) {
      trustManagers.add(load(localCerts))
    }
    trustManagers.add(systemTrustManager())

    return MergedX509TrustManager(trustManagers)
  }

  fun systemTrustManager(): X509TrustManager {
    return trustManagerForKeyStore(null)
  }

  fun includedCertificates(): List<File>? {
    var installDir: String? = System.getenv("INSTALLDIR")
    if (installDir == null) {
      installDir = "."
    }

    return File(installDir, "certificates").listFiles { f -> f.name.endsWith(".crt") }?.toList()
  }
}
